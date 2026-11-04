package com.staynest.dto;

import com.staynest.entity.Amenity;
import com.staynest.entity.Category;
import com.staynest.entity.Property;
import com.staynest.entity.PropertyImage;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class PropertyResponse {
    private Long id;
    private String title;
    private String description;
    private String propertyType;
    private String address;
    private String city;
    private String state;
    private String country;
    private String zipCode;
    private Double latitude;
    private Double longitude;
    private BigDecimal pricePerNight;
    private BigDecimal cleaningFee;
    private BigDecimal serviceFeePercent;
    private Integer maxGuests;
    private Integer bedrooms;
    private Integer beds;
    private Double bathrooms;
    private Double rating;
    private Integer reviewCount;
    private Boolean featured;
    private Boolean active;
    private String primaryImageUrl;
    private List<String> images = new ArrayList<>();
    private Category category;
    private UserProfileDto host;
    private Set<Amenity> amenities;
    private boolean favorite = false;
    private LocalDateTime createdAt;

    public PropertyResponse() {}

    public static PropertyResponse fromEntity(Property property) {
        if (property == null) return null;

        PropertyResponse dto = new PropertyResponse();
        dto.setId(property.getId());
        dto.setTitle(property.getTitle());
        dto.setDescription(property.getDescription());
        dto.setPropertyType(property.getPropertyType());
        dto.setAddress(property.getAddress());
        dto.setCity(property.getCity());
        dto.setState(property.getState());
        dto.setCountry(property.getCountry());
        dto.setZipCode(property.getZipCode());
        dto.setLatitude(property.getLatitude());
        dto.setLongitude(property.getLongitude());
        dto.setPricePerNight(property.getPricePerNight());
        dto.setCleaningFee(property.getCleaningFee());
        dto.setServiceFeePercent(property.getServiceFeePercent());
        dto.setMaxGuests(property.getMaxGuests());
        dto.setBedrooms(property.getBedrooms());
        dto.setBeds(property.getBeds());
        dto.setBathrooms(property.getBathrooms());
        dto.setRating(property.getRating());
        dto.setReviewCount(property.getReviewCount());
        dto.setFeatured(property.getFeatured());
        dto.setActive(property.getActive());
        dto.setCategory(property.getCategory());
        if (property.getAmenities() != null) {
            try {
                dto.setAmenities(new java.util.HashSet<>(property.getAmenities()));
            } catch (Exception e) {
                dto.setAmenities(new java.util.HashSet<>());
            }
        } else {
            dto.setAmenities(new java.util.HashSet<>());
        }
        dto.setCreatedAt(property.getCreatedAt());

        // Primary and image list
        dto.setPrimaryImageUrl(property.getPrimaryImageUrl());
        if (property.getImages() != null) {
            try {
                for (PropertyImage img : property.getImages()) {
                    if (img != null && img.getImageUrl() != null) {
                        dto.getImages().add(img.getImageUrl());
                    }
                }
            } catch (Exception ignored) {}
        }
        if (dto.getImages().isEmpty() && dto.getPrimaryImageUrl() != null) {
            dto.getImages().add(dto.getPrimaryImageUrl());
        }

        // Host profile
        if (property.getHost() != null) {
            UserProfileDto hostDto = new UserProfileDto();
            hostDto.setId(property.getHost().getId());
            hostDto.setFirstName(property.getHost().getFirstName());
            hostDto.setLastName(property.getHost().getLastName());
            hostDto.setEmail(property.getHost().getEmail());
            hostDto.setAvatarUrl(property.getHost().getAvatarUrl());
            hostDto.setBio(property.getHost().getBio());
            hostDto.setRole(property.getHost().getRole());
            hostDto.setCreatedAt(property.getHost().getCreatedAt());
            dto.setHost(hostDto);
        }

        return dto;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPropertyType() {
        return propertyType;
    }

    public void setPropertyType(String propertyType) {
        this.propertyType = propertyType;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public BigDecimal getPricePerNight() {
        return pricePerNight;
    }

    public void setPricePerNight(BigDecimal pricePerNight) {
        this.pricePerNight = pricePerNight;
    }

    public BigDecimal getCleaningFee() {
        return cleaningFee;
    }

    public void setCleaningFee(BigDecimal cleaningFee) {
        this.cleaningFee = cleaningFee;
    }

    public BigDecimal getServiceFeePercent() {
        return serviceFeePercent;
    }

    public void setServiceFeePercent(BigDecimal serviceFeePercent) {
        this.serviceFeePercent = serviceFeePercent;
    }

    public Integer getMaxGuests() {
        return maxGuests;
    }

    public void setMaxGuests(Integer maxGuests) {
        this.maxGuests = maxGuests;
    }

    public Integer getBedrooms() {
        return bedrooms;
    }

    public void setBedrooms(Integer bedrooms) {
        this.bedrooms = bedrooms;
    }

    public Integer getBeds() {
        return beds;
    }

    public void setBeds(Integer beds) {
        this.beds = beds;
    }

    public Double getBathrooms() {
        return bathrooms;
    }

    public void setBathrooms(Double bathrooms) {
        this.bathrooms = bathrooms;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public Integer getReviewCount() {
        return reviewCount;
    }

    public void setReviewCount(Integer reviewCount) {
        this.reviewCount = reviewCount;
    }

    public Boolean getFeatured() {
        return featured;
    }

    public void setFeatured(Boolean featured) {
        this.featured = featured;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public String getPrimaryImageUrl() {
        return primaryImageUrl;
    }

    public void setPrimaryImageUrl(String primaryImageUrl) {
        this.primaryImageUrl = primaryImageUrl;
    }

    public List<String> getImages() {
        return images;
    }

    public void setImages(List<String> images) {
        this.images = images;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public UserProfileDto getHost() {
        return host;
    }

    public void setHost(UserProfileDto host) {
        this.host = host;
    }

    public Set<Amenity> getAmenities() {
        return amenities;
    }

    public void setAmenities(Set<Amenity> amenities) {
        this.amenities = amenities;
    }

    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
