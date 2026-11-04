package com.staynest.controller;

import com.staynest.dto.AdminStatsDto;
import com.staynest.dto.ApiResponse;
import com.staynest.dto.PropertyResponse;
import com.staynest.dto.ReviewResponse;
import com.staynest.dto.UserProfileDto;
import com.staynest.entity.Role;
import com.staynest.service.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<AdminStatsDto>> getPlatformStats() {
        AdminStatsDto stats = adminService.getPlatformStats();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<UserProfileDto>>> getAllUsers() {
        List<UserProfileDto> users = adminService.getAllUsers();
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    @PutMapping("/users/{id}/status")
    public ResponseEntity<ApiResponse<UserProfileDto>> updateUserStatus(
            @PathVariable Long id,
            @RequestParam boolean active) {
        UserProfileDto updated = adminService.updateUserStatus(id, active);
        String msg = active ? "User account activated." : "User account suspended.";
        return ResponseEntity.ok(ApiResponse.success(msg, updated));
    }

    @PutMapping("/users/{id}/role")
    public ResponseEntity<ApiResponse<UserProfileDto>> updateUserRole(
            @PathVariable Long id,
            @RequestParam Role role) {
        UserProfileDto updated = adminService.updateUserRole(id, role);
        return ResponseEntity.ok(ApiResponse.success("User role updated to " + role.name(), updated));
    }

    @GetMapping("/properties")
    public ResponseEntity<ApiResponse<List<PropertyResponse>>> getAllProperties() {
        List<PropertyResponse> properties = adminService.getAllProperties();
        return ResponseEntity.ok(ApiResponse.success(properties));
    }

    @PutMapping("/properties/{id}/toggle-active")
    public ResponseEntity<ApiResponse<PropertyResponse>> togglePropertyActive(@PathVariable Long id) {
        PropertyResponse property = adminService.togglePropertyActive(id);
        String msg = Boolean.TRUE.equals(property.getActive()) ? "Listing activated." : "Listing hidden/deactivated.";
        return ResponseEntity.ok(ApiResponse.success(msg, property));
    }

    @PutMapping("/properties/{id}/toggle-featured")
    public ResponseEntity<ApiResponse<PropertyResponse>> togglePropertyFeatured(@PathVariable Long id) {
        PropertyResponse property = adminService.togglePropertyFeatured(id);
        String msg = Boolean.TRUE.equals(property.getFeatured()) ? "Listing marked as featured." : "Listing removed from featured.";
        return ResponseEntity.ok(ApiResponse.success(msg, property));
    }

    @GetMapping("/reviews")
    public ResponseEntity<ApiResponse<List<ReviewResponse>>> getAllReviews() {
        List<ReviewResponse> reviews = adminService.getAllReviews();
        return ResponseEntity.ok(ApiResponse.success(reviews));
    }

    @PutMapping("/reviews/{id}/toggle-moderated")
    public ResponseEntity<ApiResponse<ReviewResponse>> toggleReviewModerated(@PathVariable Long id) {
        ReviewResponse review = adminService.toggleReviewModerated(id);
        String msg = Boolean.TRUE.equals(review.getModerated()) ? "Review flagged and hidden from public view." : "Review approved and visible.";
        return ResponseEntity.ok(ApiResponse.success(msg, review));
    }
}
