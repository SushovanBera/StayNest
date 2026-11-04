package com.staynest.controller;

import com.staynest.config.UserPrincipal;
import com.staynest.dto.ApiResponse;
import com.staynest.dto.PropertyResponse;
import com.staynest.service.FavoriteService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/favorites")
public class FavoriteController {

    private final FavoriteService favoriteService;

    public FavoriteController(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    @PostMapping("/{propertyId}/toggle")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> toggleFavorite(
            @PathVariable Long propertyId,
            @AuthenticationPrincipal UserPrincipal principal) {
        boolean isFavorited = favoriteService.toggleFavorite(propertyId, principal.getId());
        Map<String, Boolean> result = new HashMap<>();
        result.put("favorited", isFavorited);
        String msg = isFavorited ? "Saved to your Wishlist!" : "Removed from your Wishlist.";
        return ResponseEntity.ok(ApiResponse.success(msg, result));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<PropertyResponse>>> getMyFavorites(
            @AuthenticationPrincipal UserPrincipal principal) {
        List<PropertyResponse> favorites = favoriteService.getUserFavorites(principal.getId());
        return ResponseEntity.ok(ApiResponse.success(favorites));
    }

    @GetMapping("/{propertyId}/check")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> checkFavorite(
            @PathVariable Long propertyId,
            @AuthenticationPrincipal UserPrincipal principal) {
        boolean isFavorited = (principal != null) && favoriteService.isFavorite(propertyId, principal.getId());
        Map<String, Boolean> result = new HashMap<>();
        result.put("favorited", isFavorited);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
