package com.staynest.service;

import com.staynest.dto.PropertyResponse;
import com.staynest.entity.Favorite;
import com.staynest.entity.Property;
import com.staynest.entity.User;
import com.staynest.exception.ResourceNotFoundException;
import com.staynest.repository.FavoriteRepository;
import com.staynest.repository.PropertyRepository;
import com.staynest.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final PropertyRepository propertyRepository;
    private final UserRepository userRepository;

    public FavoriteService(FavoriteRepository favoriteRepository,
                           PropertyRepository propertyRepository,
                           UserRepository userRepository) {
        this.favoriteRepository = favoriteRepository;
        this.propertyRepository = propertyRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public boolean toggleFavorite(Long propertyId, Long userId) {
        Optional<Favorite> existing = favoriteRepository.findByUserIdAndPropertyId(userId, propertyId);

        if (existing.isPresent()) {
            favoriteRepository.delete(existing.get());
            return false; // Removed from favorites
        } else {
            Property property = propertyRepository.findById(propertyId)
                    .orElseThrow(() -> new ResourceNotFoundException("Property not found with id: " + propertyId));
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));

            Favorite favorite = new Favorite(user, property);
            favoriteRepository.save(favorite);
            return true; // Added to favorites
        }
    }

    @Transactional(readOnly = true)
    public List<PropertyResponse> getUserFavorites(Long userId) {
        List<Favorite> favorites = favoriteRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return favorites.stream()
                .map(f -> {
                    PropertyResponse dto = PropertyResponse.fromEntity(f.getProperty());
                    dto.setFavorite(true);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public boolean isFavorite(Long propertyId, Long userId) {
        if (userId == null) return false;
        return favoriteRepository.existsByUserIdAndPropertyId(userId, propertyId);
    }
}
