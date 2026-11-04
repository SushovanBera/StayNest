package com.staynest.controller;

import com.staynest.config.UserPrincipal;
import com.staynest.dto.ApiResponse;
import com.staynest.dto.PropertyResponse;
import com.staynest.entity.Amenity;
import com.staynest.service.BookingService;
import com.staynest.service.PropertyService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/properties")
public class PropertyController {

    private final PropertyService propertyService;
    private final BookingService bookingService;

    public PropertyController(PropertyService propertyService, BookingService bookingService) {
        this.propertyService = propertyService;
        this.bookingService = bookingService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<PropertyResponse>>> searchProperties(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String propertyType,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Integer guests,
            @RequestParam(required = false) Double minRating,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut,
            @RequestParam(required = false) List<Long> amenityIds,
            @RequestParam(required = false, defaultValue = "newest") String sortBy,
            @AuthenticationPrincipal UserPrincipal principal) {

        Long userId = (principal != null) ? principal.getId() : null;

        List<PropertyResponse> results = propertyService.searchProperties(
                query, category, propertyType, minPrice, maxPrice, guests, minRating,
                checkIn, checkOut, amenityIds, sortBy, userId
        );

        return ResponseEntity.ok(ApiResponse.success(results));
    }

    @GetMapping("/featured")
    public ResponseEntity<ApiResponse<List<PropertyResponse>>> getFeaturedProperties(
            @AuthenticationPrincipal UserPrincipal principal) {
        Long userId = (principal != null) ? principal.getId() : null;
        List<PropertyResponse> results = propertyService.getFeaturedProperties(userId);
        return ResponseEntity.ok(ApiResponse.success(results));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PropertyResponse>> getPropertyById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        Long userId = (principal != null) ? principal.getId() : null;
        PropertyResponse property = propertyService.getPropertyById(id, userId);
        return ResponseEntity.ok(ApiResponse.success(property));
    }

    @GetMapping("/{id}/similar")
    public ResponseEntity<ApiResponse<List<PropertyResponse>>> getSimilarProperties(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        Long userId = (principal != null) ? principal.getId() : null;
        List<PropertyResponse> similar = propertyService.getSimilarProperties(id, userId);
        return ResponseEntity.ok(ApiResponse.success(similar));
    }

    @GetMapping("/{id}/booked-dates")
    public ResponseEntity<ApiResponse<List<Map<String, LocalDate>>>> getBookedDates(@PathVariable Long id) {
        List<Map<String, LocalDate>> bookedDates = bookingService.getBookedDates(id);
        return ResponseEntity.ok(ApiResponse.success(bookedDates));
    }
}
