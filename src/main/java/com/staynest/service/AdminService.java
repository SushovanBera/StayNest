package com.staynest.service;

import com.staynest.dto.AdminStatsDto;
import com.staynest.dto.PropertyResponse;
import com.staynest.dto.ReviewResponse;
import com.staynest.dto.UserProfileDto;
import com.staynest.entity.BookingStatus;
import com.staynest.entity.Property;
import com.staynest.entity.Review;
import com.staynest.entity.Role;
import com.staynest.entity.User;
import com.staynest.exception.ResourceNotFoundException;
import com.staynest.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final PropertyRepository propertyRepository;
    private final BookingRepository bookingRepository;
    private final ReviewRepository reviewRepository;

    public AdminService(UserRepository userRepository,
                        PropertyRepository propertyRepository,
                        BookingRepository bookingRepository,
                        ReviewRepository reviewRepository) {
        this.userRepository = userRepository;
        this.propertyRepository = propertyRepository;
        this.bookingRepository = bookingRepository;
        this.reviewRepository = reviewRepository;
    }

    @Transactional(readOnly = true)
    public AdminStatsDto getPlatformStats() {
        AdminStatsDto stats = new AdminStatsDto();
        stats.setTotalUsers(userRepository.count());
        stats.setTotalHosts(userRepository.countByRole(Role.ROLE_HOST));
        stats.setTotalGuests(userRepository.countByRole(Role.ROLE_GUEST));
        stats.setTotalProperties(propertyRepository.count());
        stats.setActiveProperties(propertyRepository.countByActiveTrue());
        stats.setTotalBookings(bookingRepository.count());
        stats.setConfirmedBookings(bookingRepository.countByStatus(BookingStatus.CONFIRMED));

        BigDecimal revenue = bookingRepository.sumTotalPlatformRevenue();
        stats.setTotalRevenue(revenue != null ? revenue : BigDecimal.ZERO);

        return stats;
    }

    @Transactional(readOnly = true)
    public List<UserProfileDto> getAllUsers() {
        return userRepository.findAllByOrderByCreatedAtDesc().stream().map(u -> {
            UserProfileDto dto = new UserProfileDto();
            dto.setId(u.getId());
            dto.setFirstName(u.getFirstName());
            dto.setLastName(u.getLastName());
            dto.setEmail(u.getEmail());
            dto.setPhone(u.getPhone());
            dto.setRole(u.getRole());
            dto.setAvatarUrl(u.getAvatarUrl());
            dto.setBio(u.getBio());
            dto.setActive(u.isActive());
            dto.setCreatedAt(u.getCreatedAt());
            return dto;
        }).collect(Collectors.toList());
    }

    @Transactional
    public UserProfileDto updateUserStatus(Long userId, boolean active) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        user.setActive(active);
        user = userRepository.save(user);

        UserProfileDto dto = new UserProfileDto();
        dto.setId(user.getId());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole());
        dto.setActive(user.isActive());
        return dto;
    }

    @Transactional
    public UserProfileDto updateUserRole(Long userId, Role role) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        user.setRole(role);
        user = userRepository.save(user);

        UserProfileDto dto = new UserProfileDto();
        dto.setId(user.getId());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole());
        dto.setActive(user.isActive());
        return dto;
    }

    @Transactional(readOnly = true)
    public List<PropertyResponse> getAllProperties() {
        return propertyRepository.findAll().stream()
                .map(PropertyResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public PropertyResponse togglePropertyActive(Long propertyId) {
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found"));
        property.setActive(!Boolean.TRUE.equals(property.getActive()));
        Property saved = propertyRepository.save(property);
        return PropertyResponse.fromEntity(saved);
    }

    @Transactional
    public PropertyResponse togglePropertyFeatured(Long propertyId) {
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found"));
        property.setFeatured(!Boolean.TRUE.equals(property.getFeatured()));
        Property saved = propertyRepository.save(property);
        return PropertyResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<ReviewResponse> getAllReviews() {
        return reviewRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(ReviewResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public ReviewResponse toggleReviewModerated(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));
        review.setModerated(!Boolean.TRUE.equals(review.getModerated()));
        Review saved = reviewRepository.save(review);
        return ReviewResponse.fromEntity(saved);
    }
}
