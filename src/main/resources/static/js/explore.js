/**
 * StayNest Explore & Search Page Controller
 */
let currentFilters = {
  query: '',
  category: '',
  propertyType: '',
  minPrice: '',
  maxPrice: '',
  guests: '',
  minRating: '',
  checkIn: '',
  checkOut: '',
  amenityIds: [],
  sortBy: 'newest'
};

document.addEventListener('DOMContentLoaded', () => {
  parseUrlParams();
  loadFilterAmenities();
  loadFilterCategories();
  bindFilterEvents();
  fetchProperties();
});

function parseUrlParams() {
  const urlParams = new URLSearchParams(window.location.search);
  if (urlParams.has('query')) currentFilters.query = urlParams.get('query');
  if (urlParams.has('category')) currentFilters.category = urlParams.get('category');
  if (urlParams.has('propertyType')) currentFilters.propertyType = urlParams.get('propertyType');
  if (urlParams.has('minPrice')) currentFilters.minPrice = urlParams.get('minPrice');
  if (urlParams.has('maxPrice')) currentFilters.maxPrice = urlParams.get('maxPrice');
  if (urlParams.has('guests')) currentFilters.guests = urlParams.get('guests');
  if (urlParams.has('minRating')) currentFilters.minRating = urlParams.get('minRating');
  if (urlParams.has('checkIn')) currentFilters.checkIn = urlParams.get('checkIn');
  if (urlParams.has('checkOut')) currentFilters.checkOut = urlParams.get('checkOut');
  if (urlParams.has('sortBy')) currentFilters.sortBy = urlParams.get('sortBy');

  // Populate search input values
  const destInput = document.getElementById('filterDestination');
  if (destInput && currentFilters.query) destInput.value = currentFilters.query;

  const checkInInput = document.getElementById('filterCheckIn');
  if (checkInInput && currentFilters.checkIn) checkInInput.value = currentFilters.checkIn;

  const checkOutInput = document.getElementById('filterCheckOut');
  if (checkOutInput && currentFilters.checkOut) checkOutInput.value = currentFilters.checkOut;

  const guestsInput = document.getElementById('filterGuests');
  if (guestsInput && currentFilters.guests) guestsInput.value = currentFilters.guests;

  const sortSelect = document.getElementById('sortBySelect');
  if (sortSelect && currentFilters.sortBy) sortSelect.value = currentFilters.sortBy;

  const propTypeSelect = document.getElementById('filterPropertyType');
  if (propTypeSelect && currentFilters.propertyType) propTypeSelect.value = currentFilters.propertyType;

  const minPriceInput = document.getElementById('filterMinPrice');
  if (minPriceInput && currentFilters.minPrice) minPriceInput.value = currentFilters.minPrice;

  const maxPriceInput = document.getElementById('filterMaxPrice');
  if (maxPriceInput && currentFilters.maxPrice) maxPriceInput.value = currentFilters.maxPrice;
}

async function loadFilterCategories() {
  const select = document.getElementById('filterCategorySelect');
  if (!select) return;

  try {
    const res = await API.get('/categories');
    const categories = res.data;
    categories.forEach(cat => {
      const opt = document.createElement('option');
      opt.value = cat.slug;
      opt.textContent = cat.name;
      if (cat.slug === currentFilters.category) opt.selected = true;
      select.appendChild(opt);
    });
  } catch (err) {
    console.error('Failed to load categories', err);
  }
}

async function loadFilterAmenities() {
  const container = document.getElementById('amenitiesFilterCheckboxes');
  if (!container) return;

  try {
    const res = await API.get('/amenities');
    const amenities = res.data;

    container.innerHTML = amenities.map(a => `
      <div class="col-6 col-md-4 col-lg-3 mb-2">
        <div class="form-check">
          <input class="form-check-input amenity-checkbox" type="checkbox" value="${a.id}" id="amenity_${a.id}">
          <label class="form-check-label text-truncate" for="amenity_${a.id}" title="${a.name}">
            <i class="bi ${a.iconClass} me-1 text-muted"></i> ${a.name}
          </label>
        </div>
      </div>
    `).join('');
  } catch (err) {
    console.error('Failed to load amenities', err);
  }
}

function bindFilterEvents() {
  const searchBtn = document.getElementById('applyFiltersBtn');
  if (searchBtn) {
    searchBtn.addEventListener('click', applyFilters);
  }

  const clearBtn = document.getElementById('clearFiltersBtn');
  if (clearBtn) {
    clearBtn.addEventListener('click', clearAllFilters);
  }

  const sortSelect = document.getElementById('sortBySelect');
  if (sortSelect) {
    sortSelect.addEventListener('change', () => {
      currentFilters.sortBy = sortSelect.value;
      fetchProperties();
    });
  }
}

function applyFilters() {
  currentFilters.query = document.getElementById('filterDestination')?.value.trim() || '';
  currentFilters.checkIn = document.getElementById('filterCheckIn')?.value || '';
  currentFilters.checkOut = document.getElementById('filterCheckOut')?.value || '';
  currentFilters.guests = document.getElementById('filterGuests')?.value || '';
  currentFilters.category = document.getElementById('filterCategorySelect')?.value || '';
  currentFilters.propertyType = document.getElementById('filterPropertyType')?.value || '';
  currentFilters.minPrice = document.getElementById('filterMinPrice')?.value || '';
  currentFilters.maxPrice = document.getElementById('filterMaxPrice')?.value || '';
  currentFilters.minRating = document.getElementById('filterRating')?.value || '';

  // Collect checked amenities
  const checkedBoxes = document.querySelectorAll('.amenity-checkbox:checked');
  currentFilters.amenityIds = Array.from(checkedBoxes).map(cb => cb.value);

  // Close filter modal if open
  const filterModalEl = document.getElementById('filterModal');
  if (filterModalEl) {
    const modal = bootstrap.Modal.getInstance(filterModalEl);
    if (modal) modal.hide();
  }

  fetchProperties();
}

function clearAllFilters() {
  currentFilters = {
    query: '',
    category: '',
    propertyType: '',
    minPrice: '',
    maxPrice: '',
    guests: '',
    minRating: '',
    checkIn: '',
    checkOut: '',
    amenityIds: [],
    sortBy: 'newest'
  };

  const formIds = [
    'filterDestination', 'filterCheckIn', 'filterCheckOut', 'filterGuests',
    'filterCategorySelect', 'filterPropertyType', 'filterMinPrice', 'filterMaxPrice', 'filterRating'
  ];
  formIds.forEach(id => {
    const el = document.getElementById(id);
    if (el) el.value = '';
  });

  document.querySelectorAll('.amenity-checkbox').forEach(cb => cb.checked = false);

  window.history.pushState({}, document.title, window.location.pathname);
  fetchProperties();
}

async function fetchProperties() {
  const container = document.getElementById('exploreListingsContainer');
  const countLabel = document.getElementById('resultsCountLabel');
  if (!container) return;

  container.innerHTML = `
    <div class="col-12 text-center py-5">
      <div class="spinner-border text-danger" role="status">
        <span class="visually-hidden">Loading stays...</span>
      </div>
      <p class="text-muted mt-2">Searching the best stays matching your criteria...</p>
    </div>
  `;

  try {
    const res = await API.get('/properties', currentFilters);
    const properties = res.data;

    if (countLabel) {
      countLabel.textContent = `${properties.length} stay${properties.length === 1 ? '' : 's'} available`;
    }

    if (!properties || properties.length === 0) {
      container.innerHTML = `
        <div class="col-12 text-center py-5">
          <div class="mb-3">
            <i class="bi bi-search text-muted" style="font-size: 3rem;"></i>
          </div>
          <h4 class="fw-bold">No exact matches found</h4>
          <p class="text-muted">Try changing or clearing some of your filters or searching a broader destination.</p>
          <button class="btn btn-outline-dark mt-2 rounded-pill px-4" onclick="clearAllFilters()">
            Clear all filters
          </button>
        </div>
      `;
      return;
    }

    container.innerHTML = properties.map(p => renderPropertyCard(p)).join('');
  } catch (err) {
    container.innerHTML = `
      <div class="col-12 text-center py-5">
        <p class="text-danger">Failed to fetch properties. Please try again.</p>
      </div>
    `;
  }
}

function renderPropertyCard(p) {
  const isFav = p.favorite;
  return `
    <div class="col-12 col-sm-6 col-md-4 col-lg-3 mb-4">
      <div class="property-card" onclick="window.location.href='property-details.html?id=${p.id}'">
        <div class="property-card-img-wrapper">
          <img src="${p.primaryImageUrl}" alt="${p.title}" loading="lazy">
          <button class="btn-favorite-heart" title="Save to Wishlist" onclick="toggleFavoriteStay(${p.id}, event)">
            <i class="bi ${isFav ? 'bi-heart-fill text-danger' : 'bi-heart'} heart-icon-${p.id}"></i>
          </button>
          ${p.featured ? `<span class="property-tag-badge">Rare find</span>` : ''}
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
          <div class="property-card-dates">${p.propertyType} &bull; Up to ${p.maxGuests} guests</div>
          <div class="property-card-price">
            ${formatCurrency(p.pricePerNight)} <span>night</span>
          </div>
        </div>
      </div>
    </div>
  `;
}
