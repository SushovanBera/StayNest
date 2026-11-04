/**
 * StayNest API Client & Session Management
 */
const API_BASE = '/api';

const API = {
  getToken() {
    return localStorage.getItem('staynest_token');
  },

  getUser() {
    try {
      const u = localStorage.getItem('staynest_user');
      return u ? JSON.parse(u) : null;
    } catch (e) {
      return null;
    }
  },

  setSession(authData) {
    if (authData.token) {
      localStorage.setItem('staynest_token', authData.token);
    }
    const user = {
      id: authData.id,
      firstName: authData.firstName,
      lastName: authData.lastName,
      email: authData.email,
      role: authData.role,
      avatarUrl: authData.avatarUrl
    };
    localStorage.setItem('staynest_user', JSON.stringify(user));
  },

  clearSession() {
    localStorage.removeItem('staynest_token');
    localStorage.removeItem('staynest_user');
  },

  isAuthenticated() {
    return !!this.getToken();
  },

  hasRole(role) {
    const user = this.getUser();
    return user && user.role === role;
  },

  async request(endpoint, options = {}) {
    const headers = options.headers || {};
    const token = this.getToken();

    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }

    if (!(options.body instanceof FormData)) {
      headers['Content-Type'] = 'application/json';
    }

    const config = {
      ...options,
      headers
    };

    try {
      const response = await fetch(`${API_BASE}${endpoint}`, config);
      const data = await response.json();

      if (!response.ok) {
        if (response.status === 401) {
          // Token expired or invalid
          this.clearSession();
        }
        throw new Error(data.message || 'Request failed');
      }

      return data;
    } catch (err) {
      console.error(`API Error on ${endpoint}:`, err);
      throw err;
    }
  },

  get(endpoint, params = {}) {
    const query = new URLSearchParams();
    Object.keys(params).forEach(key => {
      if (params[key] !== undefined && params[key] !== null && params[key] !== '') {
        if (Array.isArray(params[key])) {
          params[key].forEach(val => query.append(key, val));
        } else {
          query.append(key, params[key]);
        }
      }
    });

    const queryString = query.toString();
    const url = queryString ? `${endpoint}?${queryString}` : endpoint;
    return this.request(url, { method: 'GET' });
  },

  post(endpoint, body) {
    return this.request(endpoint, {
      method: 'POST',
      body: JSON.stringify(body)
    });
  },

  put(endpoint, body) {
    return this.request(endpoint, {
      method: 'PUT',
      body: JSON.stringify(body)
    });
  },

  delete(endpoint) {
    return this.request(endpoint, { method: 'DELETE' });
  }
};

/**
 * Toast Notification Helper
 */
function showToast(message, type = 'success') {
  let container = document.getElementById('staynestToastContainer');
  if (!container) {
    container = document.createElement('div');
    container.id = 'staynestToastContainer';
    container.className = 'toast-container-staynest';
    document.body.appendChild(container);
  }

  const toast = document.createElement('div');
  toast.className = 'staynest-toast';
  
  const icon = type === 'success' ? 'bi-check-circle-fill text-success' :
               type === 'danger' ? 'bi-exclamation-triangle-fill text-danger' :
               'bi-info-circle-fill text-primary';

  toast.innerHTML = `
    <i class="bi ${icon} fs-5"></i>
    <span>${message}</span>
  `;

  container.appendChild(toast);

  setTimeout(() => {
    toast.style.transition = 'all 0.3s ease';
    toast.style.opacity = '0';
    toast.style.transform = 'translateY(-10px)';
    setTimeout(() => toast.remove(), 300);
  }, 4000);
}

/**
 * Currency & Formatting Helpers
 */
function formatCurrency(amount) {
  if (amount === undefined || amount === null) return '$0';
  return new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency: 'USD',
    minimumFractionDigits: 0,
    maximumFractionDigits: 0
  }).format(amount);
}

function formatDate(dateStr) {
  if (!dateStr) return '';
  const d = new Date(dateStr);
  return d.toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' });
}

function calculateNights(checkInStr, checkOutStr) {
  if (!checkInStr || !checkOutStr) return 0;
  const d1 = new Date(checkInStr);
  const d2 = new Date(checkOutStr);
  const diffTime = d2.getTime() - d1.getTime();
  const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
  return diffDays > 0 ? diffDays : 0;
}
