/**
 * StayNest Home Page Controller
 */
document.addEventListener('DOMContentLoaded', () => {
  loadCategories();
  loadFeaturedStays();
  bindHomeSearch();
});

async function loadCategories() {
  const container = document.getElementById('categoryScrollContainer');
  if (!container) return;

  try {
    const res = await API.get('/categories');
    const categories = res.data;

    container.innerHTML = `
      <button class="category-tab active" onclick="selectCategory('', this)">
        <i class="bi bi-fire"></i>
        <span>All Stays</span>
      </button>
      ${categories.map(c => `
        <button class="category-tab" onclick="selectCategory('${c.slug}', this)">
          <i class="bi ${c.icon || 'bi-house'}"></i>
          <span>${c.name}</span>
        </button>
      `).join('')}
    `;
  } catch (err) {
    console.error('Failed to load categories', err);
  }
}

function selectCategory(slug, tabElement) {
  // Update active tab style
  document.querySelectorAll('.category-tab').forEach(t => t.classList.remove('active'));
  if (tabElement) tabElement.classList.add('active');

  if (slug) {
    window.location.href = `explore.html?category=${slug}`;
  } else {
    loadFeaturedStays();
  }
}

async function loadFeaturedStays() {
  const container = document.getElementById('featuredListingsContainer');
  if (!container) return;

  container.innerHTML = `
    <div class="col-12 text-center py-5">
      <div class="spinner-border text-danger" role="status">
        <span class="visually-hidden">Loading stays...</span>
      </div>
      <p class="text-muted mt-2">Discovering unforgettable stays...</p>
    </div>
  `;

  try {
    const res = await API.get('/properties/featured');
    const properties = res.data;

    if (!properties || properties.length === 0) {
      container.innerHTML = `
        <div class="col-12 text-center py-5">
          <i class="bi bi-house-slash fs-1 text-muted"></i>
          <h4 class="mt-3">No stays found</h4>
          <p class="text-muted">Check back soon for new luxury destinations.</p>
        </div>
      `;
      return;
    }

    container.innerHTML = properties.map(p => renderPropertyCard(p)).join('');
  } catch (err) {
    container.innerHTML = `
      <div class="col-12 text-center py-5">
        <p class="text-danger">Unable to load properties at this moment.</p>
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
          ${p.featured ? `<span class="property-tag-badge">Guest favorite</span>` : ''}
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
          <div class="property-card-dates">${p.propertyType} &bull; ${p.maxGuests} guests</div>
          <div class="property-card-price">
            ${formatCurrency(p.pricePerNight)} <span>night</span>
          </div>
        </div>
      </div>
    </div>
  `;
}

function bindHomeSearch() {
  const form = document.getElementById('homeSearchForm');
  if (!form) return;

  form.addEventListener('submit', (e) => {
    e.preventDefault();
    const destination = document.getElementById('searchDestination').value.trim();
    const checkIn = document.getElementById('searchCheckIn').value;
    const checkOut = document.getElementById('searchCheckOut').value;
    const guests = document.getElementById('searchGuests').value;

    const params = new URLSearchParams();
    if (destination) params.append('query', destination);
    if (checkIn) params.append('checkIn', checkIn);
    if (checkOut) params.append('checkOut', checkOut);
    if (guests && guests > 1) params.append('guests', guests);

    window.location.href = `explore.html?${params.toString()}`;
  });
}
