/**
 * StayNest Administrator Console Controller
 */
document.addEventListener('DOMContentLoaded', () => {
  if (!API.isAuthenticated()) {
    window.location.href = 'index.html';
    return;
  }
  const user = API.getUser();
  if (user.role !== 'ROLE_ADMIN') {
    showToast('Administrator privileges required.', 'danger');
    setTimeout(() => window.location.href = 'index.html', 1500);
    return;
  }

  loadAdminStats();
  loadAllUsers();
  loadAllProperties();
  loadAllReviews();
});

async function loadAdminStats() {
  try {
    const res = await API.get('/admin/stats');
    const s = res.data;
    document.getElementById('statAdminUsers').textContent = s.totalUsers;
    document.getElementById('statAdminHosts').textContent = `${s.totalHosts} Hosts / ${s.totalGuests} Guests`;
    document.getElementById('statAdminProperties').textContent = `${s.activeProperties} / ${s.totalProperties}`;
    document.getElementById('statAdminBookings').textContent = s.totalBookings;
    document.getElementById('statAdminRevenue').textContent = formatCurrency(s.totalRevenue);
  } catch (err) {
    console.error('Failed to load admin stats', err);
  }
}

async function loadAllUsers() {
  const container = document.getElementById('adminUsersTableBody');
  if (!container) return;

  container.innerHTML = `<tr><td colspan="6" class="text-center py-4 text-muted">Loading users...</td></tr>`;

  try {
    const res = await API.get('/admin/users');
    const users = res.data || [];

    container.innerHTML = users.map(u => `
      <tr>
        <td>#${u.id}</td>
        <td>
          <div class="d-flex align-items-center gap-2">
            <img src="${u.avatarUrl || 'https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?auto=format&fit=crop&w=60&q=80'}" 
                 class="rounded-circle" style="width: 32px; height: 32px; object-fit: cover;">
            <div>
              <div class="fw-bold">${u.firstName} ${u.lastName}</div>
              <small class="text-muted">${u.email}</small>
            </div>
          </div>
        </td>
        <td>
          <select class="form-select form-select-sm" style="width: 140px;" onchange="changeUserRole(${u.id}, this.value)">
            <option value="ROLE_GUEST" ${u.role === 'ROLE_GUEST' ? 'selected' : ''}>Guest</option>
            <option value="ROLE_HOST" ${u.role === 'ROLE_HOST' ? 'selected' : ''}>Host</option>
            <option value="ROLE_ADMIN" ${u.role === 'ROLE_ADMIN' ? 'selected' : ''}>Admin</option>
          </select>
        </td>
        <td>
          <span class="badge ${u.active ? 'bg-success' : 'bg-danger'} rounded-pill">
            ${u.active ? 'Active' : 'Suspended'}
          </span>
        </td>
        <td><small class="text-muted">${formatDate(u.createdAt)}</small></td>
        <td>
          <button class="btn ${u.active ? 'btn-outline-danger' : 'btn-outline-success'} btn-sm rounded-pill" 
                  onclick="toggleUserStatus(${u.id}, ${!u.active})">
            ${u.active ? 'Suspend' : 'Activate'}
          </button>
        </td>
      </tr>
    `).join('');
  } catch (err) {
    container.innerHTML = `<tr><td colspan="6" class="text-danger text-center">${err.message}</td></tr>`;
  }
}

async function toggleUserStatus(userId, newStatus) {
  try {
    const res = await API.put(`/admin/users/${userId}/status?active=${newStatus}`);
    showToast(res.message, 'success');
    loadAdminStats();
    loadAllUsers();
  } catch (err) {
    showToast(err.message, 'danger');
  }
}

async function changeUserRole(userId, newRole) {
  try {
    const res = await API.put(`/admin/users/${userId}/role?role=${newRole}`);
    showToast(res.message, 'success');
    loadAdminStats();
    loadAllUsers();
  } catch (err) {
    showToast(err.message, 'danger');
  }
}

async function loadAllProperties() {
  const container = document.getElementById('adminPropertiesTableBody');
  if (!container) return;

  container.innerHTML = `<tr><td colspan="7" class="text-center py-4 text-muted">Loading properties...</td></tr>`;

  try {
    const res = await API.get('/admin/properties');
    const properties = res.data || [];

    container.innerHTML = properties.map(p => `
      <tr>
        <td>
          <img src="${p.primaryImageUrl}" class="rounded-3" style="width: 50px; height: 40px; object-fit: cover;">
        </td>
        <td>
          <div class="fw-bold text-truncate" style="max-width: 220px;">${p.title}</div>
          <small class="text-muted">${p.city}, ${p.country}</small>
        </td>
        <td>${p.host ? p.host.fullName : 'Unknown'}</td>
        <td class="fw-bold">${formatCurrency(p.pricePerNight)}</td>
        <td>
          <span class="badge ${p.active ? 'bg-success' : 'bg-secondary'} rounded-pill">
            ${p.active ? 'Active' : 'Inactive'}
          </span>
        </td>
        <td>
          <span class="badge ${p.featured ? 'bg-warning text-dark' : 'bg-light text-dark border'} rounded-pill">
            ${p.featured ? 'Featured' : 'Standard'}
          </span>
        </td>
        <td>
          <div class="btn-group btn-group-sm">
            <button class="btn btn-outline-secondary" onclick="togglePropertyActive(${p.id})">
              ${p.active ? 'Hide' : 'Unhide'}
            </button>
            <button class="btn btn-outline-warning" onclick="togglePropertyFeatured(${p.id})">
              <i class="bi bi-star"></i>
            </button>
            <a href="property-details.html?id=${p.id}" class="btn btn-outline-primary" target="_blank">
              <i class="bi bi-box-arrow-up-right"></i>
            </a>
          </div>
        </td>
      </tr>
    `).join('');
  } catch (err) {
    container.innerHTML = `<tr><td colspan="7" class="text-danger text-center">${err.message}</td></tr>`;
  }
}

async function togglePropertyActive(id) {
  try {
    const res = await API.put(`/admin/properties/${id}/toggle-active`);
    showToast(res.message, 'success');
    loadAdminStats();
    loadAllProperties();
  } catch (err) {
    showToast(err.message, 'danger');
  }
}

async function togglePropertyFeatured(id) {
  try {
    const res = await API.put(`/admin/properties/${id}/toggle-featured`);
    showToast(res.message, 'success');
    loadAllProperties();
  } catch (err) {
    showToast(err.message, 'danger');
  }
}

async function loadAllReviews() {
  const container = document.getElementById('adminReviewsTableBody');
  if (!container) return;

  container.innerHTML = `<tr><td colspan="6" class="text-center py-4 text-muted">Loading reviews...</td></tr>`;

  try {
    const res = await API.get('/admin/reviews');
    const reviews = res.data || [];

    if (reviews.length === 0) {
      container.innerHTML = `<tr><td colspan="6" class="text-center py-4 text-muted">No reviews submitted on platform yet.</td></tr>`;
      return;
    }

    container.innerHTML = reviews.map(r => `
      <tr>
        <td>#${r.id}</td>
        <td>${r.userName}</td>
        <td><div class="text-truncate" style="max-width: 180px;">${r.propertyTitle}</div></td>
        <td><span class="text-warning">${'★'.repeat(r.rating)}</span></td>
        <td><div class="text-truncate" style="max-width: 250px;">${r.comment}</div></td>
        <td>
          <button class="btn ${r.moderated ? 'btn-outline-success' : 'btn-outline-danger'} btn-sm rounded-pill"
                  onclick="toggleReviewModerated(${r.id})">
            ${r.moderated ? 'Unflag / Restore' : 'Flag / Hide'}
          </button>
        </td>
      </tr>
    `).join('');
  } catch (err) {
    container.innerHTML = `<tr><td colspan="6" class="text-danger text-center">${err.message}</td></tr>`;
  }
}

async function toggleReviewModerated(id) {
  try {
    const res = await API.put(`/admin/reviews/${id}/toggle-moderated`);
    showToast(res.message, 'info');
    loadAllReviews();
  } catch (err) {
    showToast(err.message, 'danger');
  }
}
