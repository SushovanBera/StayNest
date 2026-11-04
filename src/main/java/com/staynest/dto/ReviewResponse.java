package com.staynest.dto;

import com.staynest.entity.Review;
import java.time.LocalDateTime;

public class ReviewResponse {
    private Long id;
    private Long propertyId;
    private String propertyTitle;
    private Long userId;
    private String userName;
    private String userAvatar;
    private Long bookingId;
    private Integer rating;
    private String comment;
    private Boolean moderated;
    private LocalDateTime createdAt;

    public ReviewResponse() {}

    public static ReviewResponse fromEntity(Review r) {
        if (r == null) return null;

        ReviewResponse dto = new ReviewResponse();
        dto.setId(r.getId());
        if (r.getProperty() != null) {
            dto.setPropertyId(r.getProperty().getId());
            dto.setPropertyTitle(r.getProperty().getTitle());
        }
        if (r.getUser() != null) {
            dto.setUserId(r.getUser().getId());
            dto.setUserName(r.getUser().getFullName());
            dto.setUserAvatar(r.getUser().getAvatarUrl());
        }
        if (r.getBooking() != null) {
            dto.setBookingId(r.getBooking().getId());
        }
        dto.setRating(r.getRating());
        dto.setComment(r.getComment());
        dto.setModerated(r.getModerated());
        dto.setCreatedAt(r.getCreatedAt());

        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPropertyId() {
        return propertyId;
    }

    public void setPropertyId(Long propertyId) {
        this.propertyId = propertyId;
    }

    public String getPropertyTitle() {
        return propertyTitle;
    }

    public void setPropertyTitle(String propertyTitle) {
        this.propertyTitle = propertyTitle;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserAvatar() {
        return userAvatar;
    }

    public void setUserAvatar(String userAvatar) {
        this.userAvatar = userAvatar;
    }

    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Boolean getModerated() {
        return moderated;
    }

    public void setModerated(Boolean moderated) {
        this.moderated = moderated;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
