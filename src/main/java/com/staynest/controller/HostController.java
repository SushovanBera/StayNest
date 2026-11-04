package com.staynest.controller;

import com.staynest.config.UserPrincipal;
import com.staynest.dto.ApiResponse;
import com.staynest.dto.BookingResponse;
import com.staynest.dto.HostStatsDto;
import com.staynest.dto.PropertyRequest;
import com.staynest.dto.PropertyResponse;
import com.staynest.entity.BookingStatus;
import com.staynest.service.HostService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/host")
public class HostController {

    private final HostService hostService;

    public HostController(HostService hostService) {
        this.hostService = hostService;
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<HostStatsDto>> getHostStats(@AuthenticationPrincipal UserPrincipal principal) {
        HostStatsDto stats = hostService.getHostStats(principal.getId());
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @GetMapping("/properties")
    public ResponseEntity<ApiResponse<List<PropertyResponse>>> getHostProperties(@AuthenticationPrincipal UserPrincipal principal) {
        List<PropertyResponse> properties = hostService.getHostProperties(principal.getId());
        return ResponseEntity.ok(ApiResponse.success(properties));
    }

    @PostMapping("/properties")
    public ResponseEntity<ApiResponse<PropertyResponse>> createProperty(
            @Valid @RequestBody PropertyRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        PropertyResponse property = hostService.createProperty(request, principal);
        return ResponseEntity.ok(ApiResponse.success("Property listing published successfully!", property));
    }

    @PutMapping("/properties/{id}")
    public ResponseEntity<ApiResponse<PropertyResponse>> updateProperty(
            @PathVariable Long id,
            @Valid @RequestBody PropertyRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        PropertyResponse property = hostService.updateProperty(id, request, principal);
        return ResponseEntity.ok(ApiResponse.success("Property listing updated successfully.", property));
    }

    @DeleteMapping("/properties/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProperty(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        hostService.deleteProperty(id, principal);
        return ResponseEntity.ok(ApiResponse.success("Property listing deleted successfully.", null));
    }

    @GetMapping("/reservations")
    public ResponseEntity<ApiResponse<List<BookingResponse>>> getHostReservations(@AuthenticationPrincipal UserPrincipal principal) {
        List<BookingResponse> reservations = hostService.getHostReservations(principal.getId());
        return ResponseEntity.ok(ApiResponse.success(reservations));
    }

    @PutMapping("/reservations/{bookingId}/status")
    public ResponseEntity<ApiResponse<BookingResponse>> updateReservationStatus(
            @PathVariable Long bookingId,
            @RequestParam BookingStatus status,
            @AuthenticationPrincipal UserPrincipal principal) {
        BookingResponse updated = hostService.updateReservationStatus(bookingId, status, principal);
        return ResponseEntity.ok(ApiResponse.success("Reservation status updated to " + status.name(), updated));
    }
}
