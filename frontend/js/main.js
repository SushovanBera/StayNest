/**
 * StayNest Main Navigation & Global Modal Handlers
 */
document.addEventListener('DOMContentLoaded', () => {
  renderNavbarAuth();
  bindAuthModalEvents();
});

function renderNavbarAuth() {
  const userNavContainer = document.getElementById('userNavContainer');
  if (!userNavContainer) return;

  const user = API.getUser();

  if (user) {
    const isHost = user.role === 'ROLE_HOST' || user.role === 'ROLE_ADMIN';
    const isAdmin = user.role === 'ROLE_ADMIN';

    userNavContainer.innerHTML = `
      ${!isHost ? `
        <button class="btn-become-host d-none d-md-block" onclick="switchHostMode()">
          Switch to Hosting
        </button>
      ` : `
        <a href="host-dashboard.html" class="btn-become-host d-none d-md-block">
          <i class="bi bi-speedometer2 me-1"></i> Host Dashboard
        </a>
      `}
      <div class="dropdown">
        <button class="user-profile-btn dropdown-toggle" type="button" data-bs-toggle="dropdown" aria-expanded="false">
          <i class="bi bi-list fs-5"></i>
          <img src="${user.avatarUrl || 'https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?auto=format&fit=crop&w=100&q=80'}" 
               alt="${user.firstName}" class="user-avatar-img">
        </button>
        <ul class="dropdown-menu dropdown-menu-end shadow-lg border-0 rounded-4 mt-2 p-2">
          <li class="px-3 py-2 border-bottom">
            <div class="fw-bold">${user.firstName} ${user.lastName}</div>
            <div class="text-muted small text-truncate" style="max-width: 180px;">${user.email}</div>
            <span class="badge badge-coral mt-1">${user.role.replace('ROLE_', '')}</span>
          </li>
          <li><a class="dropdown-item py-2" href="wishlist.html"><i class="bi bi-heart me-2 text-danger"></i> Wishlists</a></li>
          <li><a class="dropdown-item py-2" href="bookings.html"><i class="bi bi-suitcase-lg me-2 text-primary"></i> My Trips & Bookings</a></li>
          ${isHost ? `<li><a class="dropdown-item py-2" href="host-dashboard.html"><i class="bi bi-house-door me-2 text-success"></i> Host Dashboard</a></li>` : ''}
          ${isAdmin ? `<li><a class="dropdown-item py-2" href="admin-dashboard.html"><i class="bi bi-shield-check me-2 text-warning"></i> Admin Console</a></li>` : ''}
          <li><hr class="dropdown-divider"></li>
          <li><a class="dropdown-item py-2 text-danger" href="javascript:void(0)" onclick="handleLogout()"><i class="bi bi-box-arrow-right me-2"></i> Log out</a></li>
        </ul>
      </div>
    `;
  } else {
    userNavContainer.innerHTML = `
      <button class="btn-become-host d-none d-md-block" onclick="openAuthModal('login')">
        StayNest your home
      </button>
      <button class="user-profile-btn" onclick="openAuthModal('login')">
        <i class="bi bi-list fs-5"></i>
        <i class="bi bi-person-circle fs-5 text-secondary"></i>
      </button>
    `;
  }
}

function openAuthModal(defaultTab = 'login') {
  const modalEl = document.getElementById('authModal');
  if (!modalEl) return;

  const modal = bootstrap.Modal.getOrCreateInstance(modalEl);
  if (defaultTab === 'register') {
    document.getElementById('register-tab').click();
  } else {
    document.getElementById('login-tab').click();
  }
  modal.show();
}

function bindAuthModalEvents() {
  const loginForm = document.getElementById('loginForm');
  if (loginForm) {
    loginForm.addEventListener('submit', async (e) => {
      e.preventDefault();
      const email = document.getElementById('loginEmail').value;
      const password = document.getElementById('loginPassword').value;
      const btn = document.getElementById('loginSubmitBtn');

      try {
        btn.disabled = true;
        btn.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>Signing in...';

        const res = await API.post('/auth/login', { email, password });
        API.setSession(res.data);
        showToast(`Welcome back, ${res.data.firstName}!`, 'success');

        const modalEl = document.getElementById('authModal');
        const modal = bootstrap.Modal.getInstance(modalEl);
        if (modal) modal.hide();

        renderNavbarAuth();
        setTimeout(() => window.location.reload(), 600);
      } catch (err) {
        showToast(err.message, 'danger');
      } finally {
        btn.disabled = false;
        btn.innerHTML = 'Continue';
      }
    });
  }

  const registerForm = document.getElementById('registerForm');
  if (registerForm) {
    registerForm.addEventListener('submit', async (e) => {
      e.preventDefault();
      const firstName = document.getElementById('regFirstName').value;
      const lastName = document.getElementById('regLastName').value;
      const email = document.getElementById('regEmail').value;
      const password = document.getElementById('regPassword').value;
      const role = document.getElementById('regRole').value;
      const btn = document.getElementById('registerSubmitBtn');

      try {
        btn.disabled = true;
        btn.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>Creating account...';

        const res = await API.post('/auth/register', {
          firstName,
          lastName,
          email,
          password,
          role
        });

        API.setSession(res.data);
        showToast('Account created successfully! Welcome to StayNest.', 'success');

        const modalEl = document.getElementById('authModal');
        const modal = bootstrap.Modal.getInstance(modalEl);
        if (modal) modal.hide();

        renderNavbarAuth();
        setTimeout(() => window.location.reload(), 600);
      } catch (err) {
        showToast(err.message, 'danger');
      } finally {
        btn.disabled = false;
        btn.innerHTML = 'Sign Up';
      }
    });
  }
}

/**
 * Quick Fill Demo Credentials for Evaluators / Demonstrations
 */
function fillDemoAccount(type) {
  if (type === 'guest') {
    document.getElementById('loginEmail').value = 'guest@staynest.com';
    document.getElementById('loginPassword').value = 'guest123';
  } else if (type === 'host') {
    document.getElementById('loginEmail').value = 'host@staynest.com';
    document.getElementById('loginPassword').value = 'host123';
  } else if (type === 'admin') {
    document.getElementById('loginEmail').value = 'admin@staynest.com';
    document.getElementById('loginPassword').value = 'admin123';
  }
}

async function switchHostMode() {
  if (!API.isAuthenticated()) {
    openAuthModal('login');
    return;
  }

  try {
    const res = await API.post('/auth/switch-role?role=ROLE_HOST');
    API.setSession(res.data);
    showToast('Your account is now upgraded to Host! Redirecting to host dashboard...', 'success');
    setTimeout(() => {
      window.location.href = 'host-dashboard.html';
    }, 1000);
  } catch (err) {
    showToast(err.message, 'danger');
  }
}

function handleLogout() {
  API.clearSession();
  showToast('You have been logged out successfully.', 'info');
  setTimeout(() => {
    window.location.href = 'index.html';
  }, 600);
}

/**
 * Global Favorite Heart Toggle Handler
 */
async function toggleFavoriteStay(propertyId, event) {
  if (event) {
    event.stopPropagation();
    event.preventDefault();
  }

  if (!API.isAuthenticated()) {
    showToast('Please sign in to save stays to your Wishlist.', 'info');
    openAuthModal('login');
    return;
  }

  try {
    const res = await API.post(`/favorites/${propertyId}/toggle`);
    const isFavorited = res.data.favorited;

    // Update heart icons across the page matching this property
    const hearts = document.querySelectorAll(`.heart-icon-${propertyId}`);
    hearts.forEach(h => {
      if (isFavorited) {
        h.classList.remove('bi-heart');
        h.classList.add('bi-heart-fill', 'text-danger');
      } else {
        h.classList.remove('bi-heart-fill', 'text-danger');
        h.classList.add('bi-heart');
      }
    });

    showToast(res.message, 'success');
  } catch (err) {
    showToast(err.message, 'danger');
  }
}
