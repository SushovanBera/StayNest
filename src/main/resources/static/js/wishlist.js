/**
 * StayNest Wishlist / Favorites Controller
 */
document.addEventListener('DOMContentLoaded', () => {
  if (!API.isAuthenticated()) {
    window.location.href = 'index.html';
    return;
  }
  loadWishlist();
});

async function loadWishlist() {
  const container = document.getElementById('wishlistContainer');
  if (!container) return;

  container.innerHTML = `
    <div class="col-12 text-center py-5">
      <div class="spinner-border text-danger" role="status"></div>
      <p class="text-muted mt-2">Loading your saved stays...</p>
    </div>
  `;

  try {
    const res = await API.get('/favorites');
    const properties = res.data;

    if (!properties || properties.length === 0) {
      container.innerHTML = `
        <div class="col-12 text-center py-5">
          <div class="mb-3">
            <i class="bi bi-heart text-danger" style="font-size: 3.5rem;"></i>
          </div>
          <h4 class="fw-bold">Your wishlist is empty</h4>
          <p class="text-muted">As you search, tap the heart icon to save your favorite stays and experiences.</p>
          <a href="explore.html" class="btn btn-primary rounded-pill px-4 mt-2">
            Explore Stays
          </a>
        </div>
      `;
      return;
    }

    container.innerHTML = properties.map(p => `
      <div class="col-12 col-sm-6 col-md-4 col-lg-3 mb-4" id="wishlist-item-${p.id}">
        <div class="property-card" onclick="window.location.href='property-details.html?id=${p.id}'">
          <div class="property-card-img-wrapper">
            <img src="${p.primaryImageUrl}" alt="${p.title}" loading="lazy">
            <button class="btn-favorite-heart active" title="Remove from Wishlist" onclick="removeWishlistItem(${p.id}, event)">
              <i class="bi bi-heart-fill text-danger"></i>
            </button>
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
    `).join('');
  } catch (err) {
    container.innerHTML = `<div class="col-12 text-danger text-center">${err.message}</div>`;
  }
}

async function removeWishlistItem(propertyId, event) {
  event.stopPropagation();
  event.preventDefault();

  try {
    await API.post(`/favorites/${propertyId}/toggle`);
    const card = document.getElementById(`wishlist-item-${propertyId}`);
    if (card) {
      card.style.transition = 'all 0.3s ease';
      card.style.opacity = '0';
      card.style.transform = 'scale(0.9)';
      setTimeout(() => {
        card.remove();
        const container = document.getElementById('wishlistContainer');
        if (container && container.children.length === 0) {
          loadWishlist();
        }
      }, 300);
    }
    showToast('Removed from your wishlist.', 'info');
  } catch (err) {
    showToast(err.message, 'danger');
  }
}
