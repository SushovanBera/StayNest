/**
 * StayNest Guest Trips & Bookings Controller
 */
let allUserBookings = [];
let currentTab = 'upcoming';

document.addEventListener('DOMContentLoaded', () => {
  if (!API.isAuthenticated()) {
    window.location.href = 'index.html';
    return;
  }
  loadUserBookings();
});

async function loadUserBookings() {
  const container = document.getElementById('bookingsListContainer');
  if (!container) return;

  container.innerHTML = `
    <div class="text-center py-5">
      <div class="spinner-border text-danger" role="status"></div>
      <p class="text-muted mt-2">Loading your reservations...</p>
    </div>
  `;

  try {
    const res = await API.get('/bookings/my');
    allUserBookings = res.data || [];
    filterBookings(currentTab);
  } catch (err) {
    container.innerHTML = `<div class="alert alert-danger">${err.message}</div>`;
  }
}

function filterBookings(tab) {
  currentTab = tab;
  document.querySelectorAll('.booking-tab-btn').forEach(b => b.classList.remove('active'));
  document.getElementById(`tab-${tab}`)?.classList.add('active');

  const container = document.getElementById('bookingsListContainer');
  const today = new Date().toISOString().split('T')[0];

  let filtered = [];
  if (tab === 'upcoming') {
    filtered = allUserBookings.filter(b => b.status === 'CONFIRMED' && b.checkOut >= today);
  } else if (tab === 'completed') {
    filtered = allUserBookings.filter(b => b.status === 'COMPLETED' || (b.status === 'CONFIRMED' && b.checkOut < today));
  } else if (tab === 'cancelled') {
    filtered = allUserBookings.filter(b => b.status === 'CANCELLED');
  }

  if (filtered.length === 0) {
    container.innerHTML = `
      <div class="text-center py-5 bg-light rounded-4 border">
        <i class="bi bi-suitcase-lg text-muted" style="font-size: 3rem;"></i>
        <h5 class="fw-bold mt-3">No ${tab} bookings</h5>
        <p class="text-muted">Time to dust off your bags and start planning your next escape!</p>
        <a href="explore.html" class="btn btn-primary rounded-pill px-4 mt-2">Start Exploring Stays</a>
      </div>
    `;
    return;
  }

  container.innerHTML = filtered.map(b => renderBookingCard(b)).join('');
}

function renderBookingCard(b) {
  const isCancelled = b.status === 'CANCELLED';
  const isPast = b.status === 'COMPLETED';

  return `
    <div class="card border rounded-4 shadow-sm mb-4 overflow-hidden">
      <div class="row g-0">
        <div class="col-md-4 col-lg-3">
          <img src="${b.propertyImage}" class="img-fluid h-100 w-100" style="object-fit: cover; min-height: 200px;" alt="${b.propertyTitle}">
        </div>
        <div class="col-md-8 col-lg-9">
          <div class="card-body p-4 d-flex flex-column h-100">
            <div class="d-flex justify-content-between align-items-start mb-2">
              <div>
                <span class="badge ${isCancelled ? 'bg-secondary' : isPast ? 'bg-info' : 'bg-success'} rounded-pill mb-2">
                  ${b.status}
                </span>
                <h5 class="card-title fw-bold mb-1">${b.propertyTitle}</h5>
                <p class="text-muted small mb-0"><i class="bi bi-geo-alt"></i> ${b.propertyCity}, ${b.propertyCountry}</p>
              </div>
              <div class="text-end">
                <div class="fw-bold fs-5 text-dark">${formatCurrency(b.totalPrice)}</div>
                <div class="badge bg-light text-dark border">Code: ${b.bookingCode}</div>
              </div>
            </div>

            <div class="row my-3 py-3 border-top border-bottom bg-light rounded-3 px-2">
              <div class="col-6 col-sm-3 mb-2 mb-sm-0">
                <span class="text-muted small d-block">Check-in</span>
                <strong>${formatDate(b.checkIn)}</strong>
              </div>
              <div class="col-6 col-sm-3 mb-2 mb-sm-0">
                <span class="text-muted small d-block">Check-out</span>
                <strong>${formatDate(b.checkOut)}</strong>
              </div>
              <div class="col-6 col-sm-3">
                <span class="text-muted small d-block">Guests & Duration</span>
                <strong>${b.guestsCount} guests &bull; ${b.nightsCount} nights</strong>
              </div>
              <div class="col-6 col-sm-3">
                <span class="text-muted small d-block">Payment</span>
                <strong class="text-success"><i class="bi bi-check-circle-fill"></i> ${b.paymentStatus}</strong>
              </div>
            </div>

            <div class="mt-auto d-flex flex-wrap gap-2 justify-content-end">
              <button class="btn btn-outline-secondary btn-sm rounded-pill px-3" onclick="openReceiptModal('${b.bookingCode}')">
                <i class="bi bi-receipt me-1"></i> View Receipt
              </button>
              ${!isCancelled && !isPast ? `
                <button class="btn btn-outline-danger btn-sm rounded-pill px-3" onclick="confirmCancelBooking(${b.id})">
                  <i class="bi bi-x-circle me-1"></i> Cancel Booking
                </button>
              ` : ''}
              ${isPast ? `
                <a href="property-details.html?id=${b.propertyId}" class="btn btn-outline-primary btn-sm rounded-pill px-3">
                  <i class="bi bi-star me-1"></i> Write Review
                </a>
              ` : ''}
              <a href="property-details.html?id=${b.propertyId}" class="btn btn-dark btn-sm rounded-pill px-3">
                View Stay
              </a>
            </div>
          </div>
        </div>
      </div>
    </div>
  `;
}

async function confirmCancelBooking(bookingId) {
  if (!confirm('Are you sure you want to cancel this booking? A refund will be processed to your payment method.')) {
    return;
  }

  try {
    const res = await API.put(`/bookings/${bookingId}/cancel`);
    showToast(res.message, 'success');
    loadUserBookings();
  } catch (err) {
    showToast(err.message, 'danger');
  }
}

function openReceiptModal(bookingCode) {
  const booking = allUserBookings.find(b => b.bookingCode === bookingCode);
  if (!booking) return;

  document.getElementById('receiptCode').textContent = booking.bookingCode;
  document.getElementById('receiptDate').textContent = formatDate(booking.createdAt);
  document.getElementById('receiptGuest').textContent = booking.guestName;
  document.getElementById('receiptProperty').textContent = booking.propertyTitle;
  document.getElementById('receiptAddress').textContent = `${booking.propertyAddress}, ${booking.propertyCity}`;
  document.getElementById('receiptDates').textContent = `${formatDate(booking.checkIn)} to ${formatDate(booking.checkOut)} (${booking.nightsCount} nights)`;
  document.getElementById('receiptGuests').textContent = `${booking.guestsCount} guests`;
  document.getElementById('receiptBasePrice').textContent = formatCurrency(booking.basePrice);
  document.getElementById('receiptCleaningFee').textContent = formatCurrency(booking.cleaningFee);
  document.getElementById('receiptServiceFee').textContent = formatCurrency(booking.serviceFee);
  document.getElementById('receiptTaxes').textContent = formatCurrency(booking.taxesAmount);
  document.getElementById('receiptTotal').textContent = formatCurrency(booking.totalPrice);
  document.getElementById('receiptStatus').textContent = booking.status;
  document.getElementById('receiptPaymentMethod').textContent = booking.paymentMethod;

  const modalEl = document.getElementById('receiptModal');
  const modal = bootstrap.Modal.getOrCreateInstance(modalEl);
  modal.show();
}

function printReceipt() {
  window.print();
}
