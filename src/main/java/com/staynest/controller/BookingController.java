package com.staynest.controller;

import com.staynest.config.UserPrincipal;
import com.staynest.dto.ApiResponse;
import com.staynest.dto.BookingRequest;
import com.staynest.dto.BookingResponse;
import com.staynest.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<BookingResponse>> createBooking(
            @Valid @RequestBody BookingRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        BookingResponse booking = bookingService.createBooking(request, principal);
        return ResponseEntity.ok(ApiResponse.success("Reservation confirmed successfully!", booking));
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<BookingResponse>>> getMyBookings(
            @AuthenticationPrincipal UserPrincipal principal) {
        List<BookingResponse> bookings = bookingService.getUserBookings(principal.getId());
        return ResponseEntity.ok(ApiResponse.success(bookings));
    }

    @GetMapping("/code/{bookingCode}")
    public ResponseEntity<ApiResponse<BookingResponse>> getBookingByCode(
            @PathVariable String bookingCode,
            @AuthenticationPrincipal UserPrincipal principal) {
        BookingResponse booking = bookingService.getBookingByCode(bookingCode, principal);
        return ResponseEntity.ok(ApiResponse.success(booking));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<BookingResponse>> cancelBooking(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        BookingResponse booking = bookingService.cancelBooking(id, principal);
        return ResponseEntity.ok(ApiResponse.success("Reservation cancelled and refund processed.", booking));
    }

    @GetMapping("/calculate-price")
    public ResponseEntity<ApiResponse<Map<String, Object>>> calculatePrice(
            @RequestParam Long propertyId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut,
            @RequestParam(required = false, defaultValue = "1") Integer guests) {
        Map<String, Object> breakdown = bookingService.calculatePriceBreakdown(propertyId, checkIn, checkOut, guests);
        return ResponseEntity.ok(ApiResponse.success(breakdown));
    }
}
