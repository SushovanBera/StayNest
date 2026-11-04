/**
 * StayNest Host Dashboard Controller
 */
let hostProperties = [];
let hostReservations = [];
let availableAmenities = [];
let availableCategories = [];
let editingPropertyId = null;

document.addEventListener('DOMContentLoaded', () => {
  if (!API.isAuthenticated()) {
    window.location.href = 'index.html';
    return;
  }
  const user = API.getUser();
  if (user.role !== 'ROLE_HOST' && user.role !== 'ROLE_ADMIN') {
    showToast('Host permissions required. You can upgrade to host anytime.', 'warning');
    setTimeout(() => window.location.href = 'index.html', 1500);
    return;
  }

  loadHostStats();
  loadMetadata();
  loadHostListings();
  loadHostReservations();
  bindHostFormEvents();
});

async function loadHostStats() {
  try {
    const res = await API.get('/host/stats');
    const stats = res.data;
    document.getElementById('statTotalListings').textContent = stats.totalListings;
    document.getElementById('statTotalReservations').textContent = stats.totalReservations;
    document.getElementById('statTotalEarnings').textContent = formatCurrency(stats.totalEarnings);
    document.getElementById('statAvgRating').textContent = stats.averageRating ? `${stats.averageRating.toFixed(2)} ★` : '5.00 ★';
  } catch (err) {
    console.error('Failed to load stats', err);
  }
}

async function loadMetadata() {
  try {
    const [catRes, amenRes] = await Promise.all([
      API.get('/categories'),
      API.get('/amenities')
    ]);
    availableCategories = catRes.data || [];
    availableAmenities = amenRes.data || [];

    // Populate category dropdown
    const catSelect = document.getElementById('propCategory');
    if (catSelect) {
      catSelect.innerHTML = '<option value="">Select Category</option>' +
        availableCategories.map(c => `<option value="${c.id}">${c.name}</option>`).join('');
    }

    // Populate amenities checklist
    const amenContainer = document.getElementById('propAmenitiesChecklist');
    if (amenContainer) {
      amenContainer.innerHTML = availableAmenities.map(a => `
        <div class="col-6 col-md-4 mb-2">
          <div class="form-check">
            <input class="form-check-input host-amenity-cb" type="checkbox" value="${a.id}" id="h_amenity_${a.id}">
            <label class="form-check-label small" for="h_amenity_${a.id}">
              <i class="bi ${a.iconClass} me-1"></i> ${a.name}
            </label>
          </div>
        </div>
      `).join('');
    }
  } catch (err) {
    console.error('Failed to load metadata', err);
  }
}

async function loadHostListings() {
  const container = document.getElementById('hostListingsTableBody');
  if (!container) return;

  container.innerHTML = `<tr><td colspan="7" class="text-center py-4 text-muted">Loading your listings...</td></tr>`;

  try {
    const res = await API.get('/host/properties');
    hostProperties = res.data || [];

    if (hostProperties.length === 0) {
      container.innerHTML = `
        <tr>
          <td colspan="7" class="text-center py-5">
            <i class="bi bi-house-add fs-1 text-muted"></i>
            <h6 class="fw-bold mt-2">No listings yet</h6>
            <p class="text-muted small">Create your first property listing to start hosting guests from around the world.</p>
            <button class="btn btn-primary btn-sm rounded-pill px-3" onclick="openAddPropertyModal()">
              <i class="bi bi-plus-lg me-1"></i> Add Property
            </button>
          </td>
        </tr>
      `;
      return;
    }

    container.innerHTML = hostProperties.map(p => `
      <tr>
        <td>
          <img src="${p.primaryImageUrl}" alt="${p.title}" class="rounded-3" style="width: 60px; height: 45px; object-fit: cover;">
        </td>
        <td>
          <div class="fw-bold text-dark text-truncate" style="max-width: 200px;">${p.title}</div>
          <small class="text-muted">${p.city}, ${p.country}</small>
        </td>
        <td>
          <span class="badge bg-light text-dark border">${p.propertyType}</span>
        </td>
        <td class="fw-bold">${formatCurrency(p.pricePerNight)}</td>
        <td>
          <span class="badge ${p.active ? 'bg-success' : 'bg-secondary'} rounded-pill">
            ${p.active ? 'Active' : 'Inactive'}
          </span>
        </td>
        <td>
          <i class="bi bi-star-fill text-warning"></i> ${p.rating ? p.rating.toFixed(2) : '5.00'} (${p.reviewCount})
        </td>
        <td>
          <div class="btn-group btn-group-sm">
            <a href="property-details.html?id=${p.id}" class="btn btn-outline-secondary" title="View Listing" target="_blank">
              <i class="bi bi-eye"></i>
            </a>
            <button class="btn btn-outline-primary" title="Edit Listing" onclick="openEditPropertyModal(${p.id})">
              <i class="bi bi-pencil"></i>
            </button>
            <button class="btn btn-outline-danger" title="Delete Listing" onclick="deleteHostProperty(${p.id})">
              <i class="bi bi-trash"></i>
            </button>
          </div>
        </td>
      </tr>
    `).join('');
  } catch (err) {
    container.innerHTML = `<tr><td colspan="7" class="text-danger text-center">${err.message}</td></tr>`;
  }
}

async function loadHostReservations() {
  const container = document.getElementById('hostReservationsTableBody');
  if (!container) return;

  container.innerHTML = `<tr><td colspan="7" class="text-center py-4 text-muted">Loading reservations...</td></tr>`;

  try {
    const res = await API.get('/host/reservations');
    hostReservations = res.data || [];

    if (hostReservations.length === 0) {
      container.innerHTML = `
        <tr>
          <td colspan="7" class="text-center py-4 text-muted">
            No reservations received yet. Bookings will show up here in real time.
          </td>
        </tr>
      `;
      return;
    }

    container.innerHTML = hostReservations.map(r => `
      <tr>
        <td><strong class="badge bg-light text-dark border">${r.bookingCode}</strong></td>
        <td>
          <div class="fw-bold text-dark text-truncate" style="max-width: 180px;">${r.propertyTitle}</div>
        </td>
        <td>
          <div>${r.guestName}</div>
          <small class="text-muted">${r.guestEmail}</small>
        </td>
        <td>
          <small class="d-block">${formatDate(r.checkIn)} - ${formatDate(r.checkOut)}</small>
          <span class="text-muted small">${r.nightsCount} nights &bull; ${r.guestsCount} guests</span>
        </td>
        <td class="fw-bold">${formatCurrency(r.totalPrice)}</td>
        <td>
          <span class="badge ${r.status === 'CONFIRMED' ? 'bg-success' : r.status === 'CANCELLED' ? 'bg-danger' : 'bg-info'} rounded-pill">
            ${r.status}
          </span>
        </td>
        <td>
          ${r.status === 'CONFIRMED' ? `
            <button class="btn btn-outline-danger btn-sm rounded-pill px-2 py-0" onclick="updateReservationStatus(${r.id}, 'CANCELLED')">
              Cancel
            </button>
          ` : `
            <span class="text-muted small">No action</span>
          `}
        </td>
      </tr>
    `).join('');
  } catch (err) {
    container.innerHTML = `<tr><td colspan="7" class="text-danger text-center">${err.message}</td></tr>`;
  }
}

function openAddPropertyModal() {
  editingPropertyId = null;
  document.getElementById('propertyModalTitle').textContent = 'Add New Property Listing';
  document.getElementById('propertyForm').reset();
  document.querySelectorAll('.host-amenity-cb').forEach(cb => cb.checked = false);

  const modalEl = document.getElementById('propertyModal');
  const modal = bootstrap.Modal.getOrCreateInstance(modalEl);
  modal.show();
}

function openEditPropertyModal(id) {
  const p = hostProperties.find(item => item.id === id);
  if (!p) return;

  editingPropertyId = id;
  document.getElementById('propertyModalTitle').textContent = 'Edit Property Listing';

  document.getElementById('propTitle').value = p.title || '';
  document.getElementById('propDescription').value = p.description || '';
  document.getElementById('propCategory').value = p.category?.id || '';
  document.getElementById('propType').value = p.propertyType || 'Apartment';
  document.getElementById('propAddress').value = p.address || '';
  document.getElementById('propCity').value = p.city || '';
  document.getElementById('propState').value = p.state || '';
  document.getElementById('propCountry').value = p.country || '';
  document.getElementById('propZipCode').value = p.zipCode || '';
  document.getElementById('propPrice').value = p.pricePerNight || '';
  document.getElementById('propCleaningFee').value = p.cleaningFee || '';
  document.getElementById('propMaxGuests').value = p.maxGuests || 1;
  document.getElementById('propBedrooms').value = p.bedrooms || 1;
  document.getElementById('propBeds').value = p.beds || 1;
  document.getElementById('propBathrooms').value = p.bathrooms || 1.0;

  // Images
  const images = p.images && p.images.length > 0 ? p.images.join('\n') : p.primaryImageUrl;
  document.getElementById('propImageUrls').value = images;

  // Amenities
  const propAmenityIds = p.amenities ? p.amenities.map(a => a.id) : [];
  document.querySelectorAll('.host-amenity-cb').forEach(cb => {
    cb.checked = propAmenityIds.includes(parseInt(cb.value, 10));
  });

  const modalEl = document.getElementById('propertyModal');
  const modal = bootstrap.Modal.getOrCreateInstance(modalEl);
  modal.show();
}

function bindHostFormEvents() {
  const form = document.getElementById('propertyForm');
  if (!form) return;

  form.addEventListener('submit', async (e) => {
    e.preventDefault();
    const btn = document.getElementById('savePropertyBtn');

    const title = document.getElementById('propTitle').value.trim();
    const description = document.getElementById('propDescription').value.trim();
    const categoryId = parseInt(document.getElementById('propCategory').value, 10);
    const propertyType = document.getElementById('propType').value;
    const address = document.getElementById('propAddress').value.trim();
    const city = document.getElementById('propCity').value.trim();
    const state = document.getElementById('propState').value.trim();
    const country = document.getElementById('propCountry').value.trim();
    const zipCode = document.getElementById('propZipCode').value.trim();
    const pricePerNight = parseFloat(document.getElementById('propPrice').value);
    const cleaningFee = parseFloat(document.getElementById('propCleaningFee').value) || 0;
    const maxGuests = parseInt(document.getElementById('propMaxGuests').value, 10);
    const bedrooms = parseInt(document.getElementById('propBedrooms').value, 10);
    const beds = parseInt(document.getElementById('propBeds').value, 10);
    const bathrooms = parseFloat(document.getElementById('propBathrooms').value);

    // Collect amenities
    const amenityIds = Array.from(document.querySelectorAll('.host-amenity-cb:checked'))
      .map(cb => parseInt(cb.value, 10));

    // Images
    const imgText = document.getElementById('propImageUrls').value.trim();
    const imageUrls = imgText.split('\n').map(s => s.trim()).filter(s => s.length > 0);

    const payload = {
      title,
      description,
      categoryId,
      propertyType,
      address,
      city,
      state,
      country,
      zipCode,
      pricePerNight,
      cleaningFee,
      maxGuests,
      bedrooms,
      beds,
      bathrooms,
      amenityIds,
      imageUrls
    };

    try {
      btn.disabled = true;
      btn.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>Saving...';

      if (editingPropertyId) {
        await API.put(`/host/properties/${editingPropertyId}`, payload);
        showToast('Property updated successfully!', 'success');
      } else {
        await API.post('/host/properties', payload);
        showToast('Property published successfully!', 'success');
      }

      const modalEl = document.getElementById('propertyModal');
      const modal = bootstrap.Modal.getInstance(modalEl);
      if (modal) modal.hide();

      loadHostStats();
      loadHostListings();
    } catch (err) {
      showToast(err.message, 'danger');
    } finally {
      btn.disabled = false;
      btn.innerHTML = 'Save & Publish';
    }
  });
}

async function deleteHostProperty(id) {
  if (!confirm('Are you sure you want to deactivate and remove this property listing?')) {
    return;
  }

  try {
    await API.delete(`/host/properties/${id}`);
    showToast('Listing removed successfully.', 'info');
    loadHostStats();
    loadHostListings();
  } catch (err) {
    showToast(err.message, 'danger');
  }
}

async function updateReservationStatus(bookingId, status) {
  if (!confirm(`Are you sure you want to change reservation status to ${status}?`)) {
    return;
  }

  try {
    await API.put(`/host/reservations/${bookingId}/status?status=${status}`);
    showToast('Reservation status updated.', 'success');
    loadHostStats();
    loadHostReservations();
  } catch (err) {
    showToast(err.message, 'danger');
  }
}
