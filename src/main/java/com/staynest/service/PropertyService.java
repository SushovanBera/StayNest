
package com.staynest.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.staynest.dto.PropertyResponse;
import com.staynest.entity.Amenity;
import com.staynest.entity.Property;
import com.staynest.exception.ResourceNotFoundException;
import com.staynest.repository.AmenityRepository;
import com.staynest.repository.FavoriteRepository;
import com.staynest.repository.PropertyRepository;

@Service
public class PropertyService {

    private final PropertyRepository propertyRepository;
    private final FavoriteRepository favoriteRepository;
    private final AmenityRepository amenityRepository;

    public PropertyService(
            PropertyRepository propertyRepository,
            FavoriteRepository favoriteRepository,
            AmenityRepository amenityRepository) {

        this.propertyRepository = propertyRepository;
        this.favoriteRepository = favoriteRepository;
        this.amenityRepository = amenityRepository;
    }

    // Initialize lazy-loaded collections before creating the response
    private void initializeProperty(Property property) {

        Hibernate.initialize(property.getAmenities());
        Hibernate.initialize(property.getImages());
    }

    // Convert Property entity into PropertyResponse
    private PropertyResponse convertToResponse(
            Property property, Long currentUserId) {

        initializeProperty(property);

        PropertyResponse dto = PropertyResponse.fromEntity(property);

        if (currentUserId != null) {
            dto.setFavorite(
                favoriteRepository.existsByUserIdAndPropertyId(
                    currentUserId,
                    property.getId()
                )
            );
        }

        return dto;
    }

    @Transactional(readOnly = true)
    public List<PropertyResponse> searchProperties(
            String query,
            String categorySlug,
            String propertyType,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Integer guests,
            Double minRating,
            LocalDate checkIn,
            LocalDate checkOut,
            List<Long> amenityIds,
            String sortBy,
            Long currentUserId) {

        List<Property> properties = propertyRepository.searchProperties(
                query,
                categorySlug,
                propertyType,
                minPrice,
                maxPrice,
                guests,
                minRating,
                checkIn,
                checkOut
        );

        // Filter by amenities
        if (amenityIds != null && !amenityIds.isEmpty()) {

            properties = properties.stream()
                    .filter(property -> {

                        initializeProperty(property);

                        Set<Long> propertyAmenityIds =
                                property.getAmenities().stream()
                                        .map(Amenity::getId)
                                        .collect(Collectors.toSet());

                        return propertyAmenityIds.containsAll(amenityIds);
                    })
                    .collect(Collectors.toList());
        }

        // Sorting
        if (sortBy != null) {

            switch (sortBy.toLowerCase()) {

                case "price_asc":
                    properties.sort(
                        Comparator.comparing(Property::getPricePerNight)
                    );
                    break;

                case "price_desc":
                    properties.sort(
                        Comparator.comparing(Property::getPricePerNight)
                                .reversed()
                    );
                    break;

                case "rating":
                    properties.sort(
                        Comparator.comparing(Property::getRating)
                                .reversed()
                    );
                    break;

                case "popularity":
                    properties.sort(
                        Comparator.comparing(Property::getReviewCount)
                                .reversed()
                    );
                    break;

                case "newest":
                default:
                    properties.sort(
                        Comparator.comparing(Property::getCreatedAt)
                                .reversed()
                    );
                    break;
            }
        }

        return properties.stream()
                .map(property -> convertToResponse(property, currentUserId))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PropertyResponse getPropertyById(
            Long id, Long currentUserId) {

        Property property = propertyRepository.findById(id)
                .orElseThrow(() ->
                    new ResourceNotFoundException(
                        "Property not found with id: " + id
                    )
                );

        return convertToResponse(property, currentUserId);
    }

    @Transactional(readOnly = true)
    public List<PropertyResponse> getFeaturedProperties(
            Long currentUserId) {

        List<Property> properties =
                propertyRepository.findByFeaturedTrueAndActiveTrue();

        return properties.stream()
                .map(property -> convertToResponse(property, currentUserId))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PropertyResponse> getSimilarProperties(
            Long propertyId, Long currentUserId) {

        Property target = propertyRepository.findById(propertyId)
                .orElseThrow(() ->
                    new ResourceNotFoundException("Property not found")
                );

        Long targetCategoryId = target.getCategory() != null
                ? target.getCategory().getId()
                : null;

        List<Property> candidates =
                propertyRepository.findByActiveTrueOrderByCreatedAtDesc();

        return candidates.stream()
                .filter(property -> !property.getId().equals(propertyId))
                .filter(property -> {

                    boolean sameCategory =
                            targetCategoryId != null
                            && property.getCategory() != null
                            && targetCategoryId.equals(
                                property.getCategory().getId()
                            );

                    boolean sameCity =
                            property.getCity() != null
                            && target.getCity() != null
                            && property.getCity().equalsIgnoreCase(
                                target.getCity()
                            );

                    return sameCategory || sameCity;
                })
                .limit(4)
                .map(property -> convertToResponse(property, currentUserId))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Amenity> getAllAmenities() {

        return amenityRepository.findAllByOrderByCategoryAscNameAsc();
    }
}
