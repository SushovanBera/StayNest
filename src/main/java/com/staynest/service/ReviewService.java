package com.staynest.service;

import com.staynest.config.UserPrincipal;
import com.staynest.dto.ReviewRequest;
import com.staynest.dto.ReviewResponse;
import com.staynest.entity.Booking;
import com.staynest.entity.Property;
import com.staynest.entity.Review;
import com.staynest.entity.User;
import com.staynest.exception.BadRequestException;
import com.staynest.exception.ResourceNotFoundException;
import com.staynest.repository.BookingRepository;
import com.staynest.repository.PropertyRepository;
import com.staynest.repository.ReviewRepository;
import com.staynest.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final PropertyRepository propertyRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;

    public ReviewService(ReviewRepository reviewRepository,
                         PropertyRepository propertyRepository,
                         UserRepository userRepository,
                         BookingRepository bookingRepository) {
        this.reviewRepository = reviewRepository;
        this.propertyRepository = propertyRepository;
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
    }

    @Transactional
    public ReviewResponse addReview(ReviewRequest request, Long userId) {
        Property property = propertyRepository.findById(request.getPropertyId())
                .orElseThrow(() -> new ResourceNotFoundException("Property not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Booking booking = null;
        if (request.getBookingId() != null) {
            booking = bookingRepository.findById(request.getBookingId())
                    .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

            if (!booking.getUser().getId().equals(userId)) {
                throw new AccessDeniedException("You can only review bookings made by your account.");
            }

            if (reviewRepository.existsByBookingId(request.getBookingId())) {
                throw new BadRequestException("You have already submitted a review for this booking.");
            }
        }

        Review review = new Review();
        review.setProperty(property);
        review.setUser(user);
        review.setBooking(booking);
        review.setRating(request.getRating());
        review.setComment(request.getComment().trim());
        review.setModerated(false);

        Review saved = reviewRepository.save(review);

        // Recalculate property average rating & review count
        Double avgRating = reviewRepository.getAverageRatingForProperty(property.getId());
        long count = reviewRepository.countByPropertyId(property.getId());

        if (avgRating != null) {
            BigDecimal rounded = BigDecimal.valueOf(avgRating).setScale(2, RoundingMode.HALF_UP);
            property.setRating(rounded.doubleValue());
        }
        property.setReviewCount((int) count);
        propertyRepository.save(property);

        return ReviewResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<ReviewResponse> getPropertyReviews(Long propertyId) {
        return reviewRepository.findByPropertyIdAndModeratedFalseOrderByCreatedAtDesc(propertyId).stream()
                .map(ReviewResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteReview(Long reviewId, UserPrincipal principal) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));

        boolean isAuthor = review.getUser().getId().equals(principal.getId());
        boolean isAdmin = principal.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAuthor && !isAdmin) {
            throw new AccessDeniedException("You do not have permission to delete this review.");
        }

        Property property = review.getProperty();
        reviewRepository.delete(review);

        // Recalculate property rating
        Double avgRating = reviewRepository.getAverageRatingForProperty(property.getId());
        long count = reviewRepository.countByPropertyId(property.getId());

        property.setRating(avgRating != null ? BigDecimal.valueOf(avgRating).setScale(2, RoundingMode.HALF_UP).doubleValue() : 0.0);
        property.setReviewCount((int) count);
        propertyRepository.save(property);
    }
}
