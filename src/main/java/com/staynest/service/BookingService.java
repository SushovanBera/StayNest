package com.staynest.service;

import com.staynest.config.UserPrincipal;
import com.staynest.dto.BookingRequest;
import com.staynest.dto.BookingResponse;
import com.staynest.entity.Booking;
import com.staynest.entity.BookingStatus;
import com.staynest.entity.Property;
import com.staynest.entity.User;
import com.staynest.exception.BadRequestException;
import com.staynest.exception.ResourceNotFoundException;
import com.staynest.repository.BookingRepository;
import com.staynest.repository.PropertyRepository;
import com.staynest.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final PropertyRepository propertyRepository;
    private final UserRepository userRepository;

    public BookingService(BookingRepository bookingRepository,
                          PropertyRepository propertyRepository,
                          UserRepository userRepository) {
        this.bookingRepository = bookingRepository;
        this.propertyRepository = propertyRepository;
        this.userRepository = userRepository;
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public BookingResponse createBooking(BookingRequest request, UserPrincipal currentUser) {
        if (request.getCheckIn().isBefore(LocalDate.now())) {
            throw new BadRequestException("Check-in date cannot be in the past.");
        }
        if (!request.getCheckOut().isAfter(request.getCheckIn())) {
            throw new BadRequestException("Check-out date must be strictly after check-in date.");
        }

        Property property = propertyRepository.findById(request.getPropertyId())
                .orElseThrow(() -> new ResourceNotFoundException("Property not found"));

        if (!Boolean.TRUE.equals(property.getActive())) {
            throw new BadRequestException("This property is currently not available for booking.");
        }

        if (request.getGuestsCount() > property.getMaxGuests()) {
            throw new BadRequestException("Guest count exceeds the property's maximum capacity of " + property.getMaxGuests() + " guests.");
        }

        // Concurrency & Overlap Safeguard: Verify no overlapping active reservations
        long conflicts = bookingRepository.countConflictingBookings(
                property.getId(), request.getCheckIn(), request.getCheckOut()
        );
        if (conflicts > 0) {
            throw new BadRequestException("The property is unavailable for the selected dates. Please select alternative dates.");
        }

        User guest = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Price breakdown calculation
        long nights = ChronoUnit.DAYS.between(request.getCheckIn(), request.getCheckOut());
        if (nights < 1) nights = 1;

        BigDecimal basePrice = property.getPricePerNight().multiply(BigDecimal.valueOf(nights));
        BigDecimal cleaningFee = property.getCleaningFee() != null ? property.getCleaningFee() : BigDecimal.ZERO;

        BigDecimal feePercent = property.getServiceFeePercent() != null ? property.getServiceFeePercent() : new BigDecimal("12.00");
        BigDecimal serviceFee = basePrice.multiply(feePercent)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        // 8% demo tax on base + cleaning + service
        BigDecimal taxableAmount = basePrice.add(cleaningFee).add(serviceFee);
        BigDecimal taxesAmount = taxableAmount.multiply(new BigDecimal("0.08"))
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal totalPrice = basePrice.add(cleaningFee).add(serviceFee).add(taxesAmount);

        // Unique Booking Confirmation Code (e.g. SN-2026-9A3F)
        String bookingCode = "SN-" + LocalDate.now().getYear() + "-" +
                UUID.randomUUID().toString().substring(0, 6).toUpperCase();

        Booking booking = new Booking();
        booking.setBookingCode(bookingCode);
        booking.setProperty(property);
        booking.setUser(guest);
        booking.setCheckIn(request.getCheckIn());
        booking.setCheckOut(request.getCheckOut());
        booking.setGuestsCount(request.getGuestsCount());
        booking.setNightsCount((int) nights);
        booking.setBasePrice(basePrice);
        booking.setCleaningFee(cleaningFee);
        booking.setServiceFee(serviceFee);
        booking.setTaxesAmount(taxesAmount);
        booking.setTotalPrice(totalPrice);
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setPaymentStatus("PAID");
        booking.setPaymentMethod(request.getPaymentMethod() != null ? request.getPaymentMethod() : "DEMO_CARD");
        booking.setSpecialRequests(request.getSpecialRequests());

        Booking saved = bookingRepository.save(booking);
        return BookingResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> getUserBookings(Long userId) {
        return bookingRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(BookingResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BookingResponse getBookingByCode(String code, UserPrincipal currentUser) {
        Booking booking = bookingRepository.findByBookingCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with code: " + code));

        boolean isGuest = booking.getUser().getId().equals(currentUser.getId());
        boolean isHost = booking.getProperty().getHost().getId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isGuest && !isHost && !isAdmin) {
            throw new AccessDeniedException("You are not authorized to view this reservation.");
        }

        return BookingResponse.fromEntity(booking);
    }

    @Transactional
    public BookingResponse cancelBooking(Long bookingId, UserPrincipal currentUser) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        boolean isGuest = booking.getUser().getId().equals(currentUser.getId());
        boolean isHost = booking.getProperty().getHost().getId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isGuest && !isHost && !isAdmin) {
            throw new AccessDeniedException("You are not authorized to cancel this booking.");
        }

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new BadRequestException("This booking is already cancelled.");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        booking.setPaymentStatus("REFUNDED");
        Booking updated = bookingRepository.save(booking);
        return BookingResponse.fromEntity(updated);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> calculatePriceBreakdown(Long propertyId, LocalDate checkIn, LocalDate checkOut, Integer guests) {
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found"));

        if (checkIn == null || checkOut == null || !checkOut.isAfter(checkIn)) {
            throw new BadRequestException("Invalid check-in or check-out date.");
        }

        long nights = ChronoUnit.DAYS.between(checkIn, checkOut);
        if (nights < 1) nights = 1;

        BigDecimal basePrice = property.getPricePerNight().multiply(BigDecimal.valueOf(nights));
        BigDecimal cleaningFee = property.getCleaningFee() != null ? property.getCleaningFee() : BigDecimal.ZERO;
        BigDecimal feePercent = property.getServiceFeePercent() != null ? property.getServiceFeePercent() : new BigDecimal("12.00");
        BigDecimal serviceFee = basePrice.multiply(feePercent).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal taxableAmount = basePrice.add(cleaningFee).add(serviceFee);
        BigDecimal taxesAmount = taxableAmount.multiply(new BigDecimal("0.08")).setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalPrice = basePrice.add(cleaningFee).add(serviceFee).add(taxesAmount);

        boolean isAvailable = bookingRepository.countConflictingBookings(propertyId, checkIn, checkOut) == 0;

        Map<String, Object> result = new HashMap<>();
        result.put("propertyId", propertyId);
        result.put("nights", nights);
        result.put("pricePerNight", property.getPricePerNight());
        result.put("basePrice", basePrice);
        result.put("cleaningFee", cleaningFee);
        result.put("serviceFee", serviceFee);
        result.put("taxesAmount", taxesAmount);
        result.put("totalPrice", totalPrice);
        result.put("isAvailable", isAvailable);

        return result;
    }

    @Transactional(readOnly = true)
    public List<Map<String, LocalDate>> getBookedDates(Long propertyId) {
        List<Booking> bookings = bookingRepository.findByPropertyIdOrderByCheckInAsc(propertyId);
        return bookings.stream()
                .filter(b -> b.getStatus() != BookingStatus.CANCELLED)
                .map(b -> {
                    Map<String, LocalDate> dates = new HashMap<>();
                    dates.put("checkIn", b.getCheckIn());
                    dates.put("checkOut", b.getCheckOut());
                    return dates;
                })
                .collect(Collectors.toList());
    }
}
