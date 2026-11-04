/**
 * StayNest Property Details & Dynamic Booking Widget Controller
 */
let currentProperty = null;
let bookedDateRanges = [];

document.addEventListener('DOMContentLoaded', () => {
  const urlParams = new URLSearchParams(window.location.search);
  const propertyId = urlParams.get('id');

  if (!propertyId) {
    window.location.href = 'explore.html';
    return;
  }

  loadPropertyDetails(propertyId);
  loadBookedDates(propertyId);
  loadPropertyReviews(propertyId);
  loadSimilarProperties(propertyId);
  bindBookingFormEvents();
});

async function loadPropertyDetails(id) {
  try {
    const res = await API.get(`/properties/${id}`);
    currentProperty = res.data;
    renderPropertyHeader();
    renderGallery();
    renderMainDetails();
    renderAmenities();
    initBookingWidget();
  } catch (err) {
    showToast('Failed to load property details: ' + err.message, 'danger');
  }
}

async function loadBookedDates(id) {
  try {
    const res = await API.get(`/properties/${id}/booked-dates`);
    bookedDateRanges = res.data || [];
  } catch (err) {
    console.warn('Unable to load booked dates', err);
  }
}

function renderPropertyHeader() {
  document.getElementById('propertyTitle').textContent = currentProperty.title;
  document.getElementById('propertyLocation').textContent = `${currentProperty.city}, ${currentProperty.state ? currentProperty.state + ', ' : ''}${currentProperty.country}`;
  document.getElementById('propertyRatingTop').innerHTML = `
    <i class="bi bi-star-fill text-warning me-1"></i>
    <span class="fw-bold">${currentProperty.rating ? currentProperty.rating.toFixed(2) : 'New'}</span>
    <span class="text-muted ms-1">(${currentProperty.reviewCount} reviews)</span>
    <span class="mx-2">&bull;</span>
    <span class="badge badge-coral">${currentProperty.category?.name || 'Vacation Stay'}</span>
  `;

  const heartBtn = document.getElementById('detailsHeartBtn');
  if (heartBtn) {
    heartBtn.innerHTML = `<i class="bi ${currentProperty.favorite ? 'bi-heart-fill text-danger' : 'bi-heart'} heart-icon-${currentProperty.id} fs-4"></i>`;
    heartBtn.onclick = (e) => toggleFavoriteStay(currentProperty.id, e);
  }
}

function renderGallery() {
  const grid = document.getElementById('propertyGalleryGrid');
  if (!grid) return;

  const images = currentProperty.images && currentProperty.images.length > 0 
    ? currentProperty.images 
    : [currentProperty.primaryImageUrl];

  // Create a 5-photo mosaic or fill remaining slots
  let html = `
    <div class="gallery-item main-photo">
      <img src="${images[0]}" alt="${currentProperty.title}" onclick="openLightbox('${images[0]}')">
    </div>
  `;

  for (let i = 1; i < 5; i++) {
    const imgUrl = images[i] || images[0];
    html += `
      <div class="gallery-item">
        <img src="${imgUrl}" alt="${currentProperty.title}" onclick="openLightbox('${imgUrl}')">
      </div>
    `;
  }

  grid.innerHTML = html;
}

function openLightbox(url) {
  window.open(url, '_blank');
}

function renderMainDetails() {
  document.getElementById('propertySpecs').textContent = 
    `${currentProperty.maxGuests} guests &bull; ${currentProperty.bedrooms} bedrooms &bull; ${currentProperty.beds} beds &bull; ${currentProperty.bathrooms} baths`;

  document.getElementById('propertyDescription').textContent = currentProperty.description;

  // Host info
  const host = currentProperty.host;
  if (host) {
    document.getElementById('hostName').textContent = `Hosted by ${host.fullName}`;
    document.getElementById('hostBio').textContent = host.bio || 'Experienced StayNest Superhost dedicated to exceptional hospitality.';
    if (host.avatarUrl) {
      document.getElementById('hostAvatar').src = host.avatarUrl;
    }
  }
}

function renderAmenities() {
  const container = document.getElementById('amenitiesGridContainer');
  if (!container || !currentProperty.amenities) return;

  if (currentProperty.amenities.length === 0) {
    container.innerHTML = '<p class="text-muted">Standard amenities provided.</p>';
    return;
  }

  container.innerHTML = currentProperty.amenities.map(a => `
    <div class="amenity-item">
      <i class="bi ${a.iconClass || 'bi-check-circle'}"></i>
      <span>${a.name}</span>
    </div>
  `).join('');
}

function initBookingWidget() {
  document.getElementById('widgetPriceNight').textContent = formatCurrency(currentProperty.pricePerNight);

  const checkInInput = document.getElementById('bookingCheckIn');
  const checkOutInput = document.getElementById('bookingCheckOut');
  const guestsSelect = document.getElementById('bookingGuests');

  // Set min date to today
  const today = new Date().toISOString().split('T')[0];
  checkInInput.min = today;

  // Default checkIn tomorrow, checkOut +3 days
  const tomorrow = new Date();
  tomorrow.setDate(tomorrow.getDate() + 1);
  const defaultCheckIn = tomorrow.toISOString().split('T')[0];

  const future = new Date();
  future.setDate(future.getDate() + 4);
  const defaultCheckOut = future.toISOString().split('T')[0];

  checkInInput.value = defaultCheckIn;
  checkOutInput.min = defaultCheckIn;
  checkOutInput.value = defaultCheckOut;

  // Populate guest dropdown based on max capacity
  guestsSelect.innerHTML = '';
  for (let i = 1; i <= currentProperty.maxGuests; i++) {
    const opt = document.createElement('option');
    opt.value = i;
    opt.textContent = `${i} guest${i > 1 ? 's' : ''}`;
    guestsSelect.appendChild(opt);
  }

  recalculateWidgetPrice();
}

function bindBookingFormEvents() {
  const checkInInput = document.getElementById('bookingCheckIn');
  const checkOutInput = document.getElementById('bookingCheckOut');
  const guestsSelect = document.getElementById('bookingGuests');

  if (checkInInput && checkOutInput) {
    checkInInput.addEventListener('change', () => {
      checkOutInput.min = checkInInput.value;
      if (checkOutInput.value <= checkInInput.value) {
        const nextDay = new Date(checkInInput.value);
        nextDay.setDate(nextDay.getDate() + 1);
        checkOutInput.value = nextDay.toISOString().split('T')[0];
      }
      recalculateWidgetPrice();
    });

    checkOutInput.addEventListener('change', recalculateWidgetPrice);
  }

  if (guestsSelect) {
    guestsSelect.addEventListener('change', recalculateWidgetPrice);
  }

  const reserveBtn = document.getElementById('btnOpenCheckout');
  if (reserveBtn) {
    reserveBtn.addEventListener('click', handleOpenCheckoutModal);
  }

  const confirmReservationBtn = document.getElementById('btnConfirmReservation');
  if (confirmReservationBtn) {
    confirmReservationBtn.addEventListener('click', submitBookingReservation);
  }
}

function checkDateConflicts(checkIn, checkOut) {
  if (!bookedDateRanges || bookedDateRanges.length === 0) return false;
  const reqStart = new Date(checkIn);
  const reqEnd = new Date(checkOut);

  return bookedDateRanges.some(range => {
    const bookedStart = new Date(range.checkIn);
    const bookedEnd = new Date(range.checkOut);
    return reqStart < bookedEnd && reqEnd > bookedStart;
  });
}

function recalculateWidgetPrice() {
  if (!currentProperty) return;

  const checkIn = document.getElementById('bookingCheckIn').value;
  const checkOut = document.getElementById('bookingCheckOut').value;
  const availabilityAlert = document.getElementById('availabilityAlert');
  const reserveBtn = document.getElementById('btnOpenCheckout');

  const nights = calculateNights(checkIn, checkOut);

  if (nights < 1) {
    document.getElementById('breakdownSection').style.display = 'none';
    if (reserveBtn) reserveBtn.disabled = true;
    return;
  }

  // Check if dates conflict with existing bookings
  const isConflict = checkDateConflicts(checkIn, checkOut);
  if (isConflict) {
    if (availabilityAlert) {
      availabilityAlert.style.display = 'block';
      availabilityAlert.textContent = 'Selected dates are already booked. Please choose different dates.';
    }
    if (reserveBtn) reserveBtn.disabled = true;
    document.getElementById('breakdownSection').style.display = 'none';
    return;
  } else {
    if (availabilityAlert) availabilityAlert.style.display = 'none';
    if (reserveBtn) reserveBtn.disabled = false;
    document.getElementById('breakdownSection').style.display = 'block';
  }

  const rate = Number(currentProperty.pricePerNight);
  const basePrice = rate * nights;
  const cleaningFee = Number(currentProperty.cleaningFee || 0);
  const serviceFee = Math.round((basePrice * (Number(currentProperty.serviceFeePercent || 12) / 100)) * 100) / 100;
  const taxes = Math.round(((basePrice + cleaningFee + serviceFee) * 0.08) * 100) / 100;
  const total = basePrice + cleaningFee + serviceFee + taxes;

  document.getElementById('calcNightsLabel').textContent = `${formatCurrency(rate)} x ${nights} night${nights > 1 ? 's' : ''}`;
  document.getElementById('calcBasePrice').textContent = formatCurrency(basePrice);
  document.getElementById('calcCleaningFee').textContent = formatCurrency(cleaningFee);
  document.getElementById('calcServiceFee').textContent = formatCurrency(serviceFee);
  document.getElementById('calcTaxes').textContent = formatCurrency(taxes);
  document.getElementById('calcTotalPrice').textContent = formatCurrency(total);
}

function handleOpenCheckoutModal() {
  if (!API.isAuthenticated()) {
    showToast('Please log in or sign up to reserve this stay.', 'info');
    openAuthModal('login');
    return;
  }

  const checkIn = document.getElementById('bookingCheckIn').value;
  const checkOut = document.getElementById('bookingCheckOut').value;
  const guests = document.getElementById('bookingGuests').value;
  const nights = calculateNights(checkIn, checkOut);

  const rate = Number(currentProperty.pricePerNight);
  const basePrice = rate * nights;
  const cleaningFee = Number(currentProperty.cleaningFee || 0);
  const serviceFee = Math.round((basePrice * (Number(currentProperty.serviceFeePercent || 12) / 100)) * 100) / 100;
  const taxes = Math.round(((basePrice + cleaningFee + serviceFee) * 0.08) * 100) / 100;
  const total = basePrice + cleaningFee + serviceFee + taxes;

  // Populate checkout modal
  document.getElementById('checkoutPropertyTitle').textContent = currentProperty.title;
  document.getElementById('checkoutPropertyThumb').src = currentProperty.primaryImageUrl;
  document.getElementById('checkoutDates').textContent = `${formatDate(checkIn)} - ${formatDate(checkOut)} (${nights} night${nights > 1 ? 's' : ''})`;
  document.getElementById('checkoutGuests').textContent = `${guests} guest${guests > 1 ? 's' : ''}`;
  document.getElementById('checkoutTotal').textContent = formatCurrency(total);

  const modalEl = document.getElementById('checkoutModal');
  const modal = bootstrap.Modal.getOrCreateInstance(modalEl);
  modal.show();
}

async function submitBookingReservation() {
  const btn = document.getElementById('btnConfirmReservation');
  const checkIn = document.getElementById('bookingCheckIn').value;
  const checkOut = document.getElementById('bookingCheckOut').value;
  const guestsCount = parseInt(document.getElementById('bookingGuests').value, 10);
  const paymentMethod = document.querySelector('input[name="demoPaymentMethod"]:checked')?.value || 'DEMO_CARD';
  const specialRequests = document.getElementById('bookingSpecialRequests')?.value || '';

  try {
    btn.disabled = true;
    btn.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>Confirming reservation...';

    const payload = {
      propertyId: currentProperty.id,
      checkIn,
      checkOut,
      guestsCount,
      paymentMethod,
      specialRequests
    };

    const res = await API.post('/bookings', payload);
    const booking = res.data;

    // Close checkout modal
    const checkoutModalEl = document.getElementById('checkoutModal');
    const modal = bootstrap.Modal.getInstance(checkoutModalEl);
    if (modal) modal.hide();

    // Show Confirmation Dialog
    showBookingSuccessModal(booking);

  } catch (err) {
    showToast(err.message, 'danger');
  } finally {
    btn.disabled = false;
    btn.innerHTML = '<i class="bi bi-shield-check me-2"></i> Confirm & Book Reservation';
  }
}

function showBookingSuccessModal(booking) {
  document.getElementById('confirmBookingCode').textContent = booking.bookingCode;
  document.getElementById('confirmPropertyTitle').textContent = booking.propertyTitle;
  document.getElementById('confirmDates').textContent = `${formatDate(booking.checkIn)} to ${formatDate(booking.checkOut)}`;
  document.getElementById('confirmTotalPrice').textContent = formatCurrency(booking.totalPrice);

  const successModalEl = document.getElementById('bookingSuccessModal');
  const successModal = bootstrap.Modal.getOrCreateInstance(successModalEl);
  successModal.show();
}

async function loadPropertyReviews(propertyId) {
  const container = document.getElementById('propertyReviewsContainer');
  if (!container) return;

  try {
    const res = await API.get(`/reviews/property/${propertyId}`);
    const reviews = res.data;

    if (!reviews || reviews.length === 0) {
      container.innerHTML = `
        <div class="col-12 py-3 text-muted">
          <p>No reviews yet for this listing. Be the first to share your experience!</p>
        </div>
      `;
      return;
    }

    container.innerHTML = reviews.map(r => `
      <div class="col-12 col-md-6 mb-4">
        <div class="review-card">
          <div class="reviewer-meta">
            <img src="${r.userAvatar || 'https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?auto=format&fit=crop&w=80&q=80'}" 
                 alt="${r.userName}" class="reviewer-avatar">
            <div>
              <div class="fw-bold">${r.userName}</div>
              <div class="text-muted small">${formatDate(r.createdAt)}</div>
            </div>
            <div class="ms-auto text-warning">
              ${'★'.repeat(r.rating)}${'☆'.repeat(5 - r.rating)}
            </div>
          </div>
          <p class="mb-0 text-dark-light">${r.comment}</p>
        </div>
      </div>
    `).join('');
  } catch (err) {
    console.error('Failed to load reviews', err);
  }
}

function openAddReviewModal() {
  if (!API.isAuthenticated()) {
    showToast('Please log in to submit a review.', 'info');
    openAuthModal('login');
    return;
  }
  const modalEl = document.getElementById('addReviewModal');
  const modal = bootstrap.Modal.getOrCreateInstance(modalEl);
  modal.show();
}

async function submitReview() {
  const rating = document.querySelector('input[name="reviewRating"]:checked')?.value;
  const comment = document.getElementById('reviewCommentText').value.trim();
  const btn = document.getElementById('submitReviewBtn');

  if (!rating) {
    showToast('Please select a star rating.', 'danger');
    return;
  }
  if (!comment) {
    showToast('Please write a comment describing your stay.', 'danger');
    return;
  }

  try {
    btn.disabled = true;
    btn.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>Posting...';

    await API.post('/reviews', {
      propertyId: currentProperty.id,
      rating: parseInt(rating, 10),
      comment
    });

    showToast('Thank you! Your review has been posted.', 'success');

    const modalEl = document.getElementById('addReviewModal');
    const modal = bootstrap.Modal.getInstance(modalEl);
    if (modal) modal.hide();

    // Reload reviews and property stats
    loadPropertyReviews(currentProperty.id);
    loadPropertyDetails(currentProperty.id);
  } catch (err) {
    showToast(err.message, 'danger');
  } finally {
    btn.disabled = false;
    btn.innerHTML = 'Post Review';
  }
}

async function loadSimilarProperties(propertyId) {
  const container = document.getElementById('similarPropertiesContainer');
  if (!container) return;

  try {
    const res = await API.get(`/properties/${propertyId}/similar`);
    const similar = res.data;

    if (!similar || similar.length === 0) {
      container.parentElement.style.display = 'none';
      return;
    }

    container.innerHTML = similar.map(p => `
      <div class="col-12 col-sm-6 col-md-3 mb-4">
        <div class="property-card" onclick="window.location.href='property-details.html?id=${p.id}'">
          <div class="property-card-img-wrapper">
            <img src="${p.primaryImageUrl}" alt="${p.title}" loading="lazy">
          </div>
          <div class="property-card-body">
            <div class="property-header-row">
              <h6 class="property-card-title">${p.title}</h6>
              <div class="property-rating-badge">
                <i class="bi bi-star-fill"></i>
                <span>${p.rating ? p.rating.toFixed(2) : 'New'}</span>
              </div>
            </div>
            <div class="property-card-subtitle">${p.city}, ${p.country}</div>
            <div class="property-card-price">${formatCurrency(p.pricePerNight)} <span>night</span></div>
          </div>
        </div>
      </div>
    `).join('');
  } catch (err) {
    console.error('Failed to load similar stays', err);
  }
}
