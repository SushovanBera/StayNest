package com.staynest.controller;

import com.staynest.config.UserPrincipal;
import com.staynest.dto.ApiResponse;
import com.staynest.dto.ReviewRequest;
import com.staynest.dto.ReviewResponse;
import com.staynest.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ReviewResponse>> addReview(
            @Valid @RequestBody ReviewRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        ReviewResponse review = reviewService.addReview(request, principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Review submitted successfully! Thank you for your feedback.", review));
    }

    @GetMapping("/property/{propertyId}")
    public ResponseEntity<ApiResponse<List<ReviewResponse>>> getPropertyReviews(@PathVariable Long propertyId) {
        List<ReviewResponse> reviews = reviewService.getPropertyReviews(propertyId);
        return ResponseEntity.ok(ApiResponse.success(reviews));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteReview(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        reviewService.deleteReview(id, principal);
        return ResponseEntity.ok(ApiResponse.success("Review deleted successfully.", null));
    }
}
