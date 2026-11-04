package com.staynest.service;

import com.staynest.config.UserPrincipal;
import com.staynest.dto.BookingResponse;
import com.staynest.dto.HostStatsDto;
import com.staynest.dto.PropertyRequest;
import com.staynest.dto.PropertyResponse;
import com.staynest.entity.*;
import com.staynest.exception.BadRequestException;
import com.staynest.exception.ResourceNotFoundException;
import com.staynest.repository.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class HostService {

    private final PropertyRepository propertyRepository;
    private final PropertyImageRepository propertyImageRepository;
    private final CategoryRepository categoryRepository;
    private final AmenityRepository amenityRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;

    public HostService(PropertyRepository propertyRepository,
                       PropertyImageRepository propertyImageRepository,
                       CategoryRepository categoryRepository,
                       AmenityRepository amenityRepository,
                       BookingRepository bookingRepository,
                       UserRepository userRepository) {
        this.propertyRepository = propertyRepository;
        this.propertyImageRepository = propertyImageRepository;
        this.categoryRepository = categoryRepository;
        this.amenityRepository = amenityRepository;
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public PropertyResponse createProperty(PropertyRequest request, UserPrincipal hostPrincipal) {
        User host = userRepository.findById(hostPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Host user not found"));

        // Automatically upgrade to HOST if user was GUEST
        if (host.getRole() == Role.ROLE_GUEST) {
            host.setRole(Role.ROLE_HOST);
            userRepository.save(host);
        }

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new BadRequestException("Invalid category ID: " + request.getCategoryId()));

        Property property = new Property();
        property.setHost(host);
        property.setCategory(category);
        populatePropertyDetails(property, request);
        property.setActive(true);
        property.setFeatured(false);
        property.setRating(5.0); // Initial 5-star baseline for new verified listing
        property.setReviewCount(0);

        Property savedProperty = propertyRepository.save(property);

        // Save and attach images
        List<PropertyImage> savedImages = savePropertyImages(savedProperty, request.getImageUrls());
        savedProperty.setImages(savedImages);

        return PropertyResponse.fromEntity(savedProperty);
    }

    @Transactional
    public PropertyResponse updateProperty(Long propertyId, PropertyRequest request, UserPrincipal hostPrincipal) {
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found with id: " + propertyId));

        boolean isHost = property.getHost().getId().equals(hostPrincipal.getId());
        boolean isAdmin = hostPrincipal.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isHost && !isAdmin) {
            throw new AccessDeniedException("You are not authorized to edit this property listing.");
        }

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new BadRequestException("Invalid category ID: " + request.getCategoryId()));

        property.setCategory(category);
        populatePropertyDetails(property, request);

        Property updatedProperty = propertyRepository.save(property);

        if (request.getImageUrls() != null && !request.getImageUrls().isEmpty()) {
            propertyImageRepository.deleteByPropertyId(property.getId());
            List<PropertyImage> updatedImages = savePropertyImages(updatedProperty, request.getImageUrls());
            updatedProperty.setImages(updatedImages);
        }

        return PropertyResponse.fromEntity(updatedProperty);
    }

    @Transactional
    public void deleteProperty(Long propertyId, UserPrincipal hostPrincipal) {
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found"));

        boolean isHost = property.getHost().getId().equals(hostPrincipal.getId());
        boolean isAdmin = hostPrincipal.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isHost && !isAdmin) {
            throw new AccessDeniedException("You are not authorized to delete this property listing.");
        }

        // Soft delete to protect existing booking records
        property.setActive(false);
        propertyRepository.save(property);
    }

    @Transactional(readOnly = true)
    public List<PropertyResponse> getHostProperties(Long hostId) {
        return propertyRepository.findByHostIdOrderByCreatedAtDesc(hostId).stream()
                .map(PropertyResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> getHostReservations(Long hostId) {
        return bookingRepository.findByPropertyHostIdOrderByCreatedAtDesc(hostId).stream()
                .map(BookingResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public BookingResponse updateReservationStatus(Long bookingId, BookingStatus newStatus, UserPrincipal hostPrincipal) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + bookingId));

        boolean isHost = booking.getProperty().getHost().getId().equals(hostPrincipal.getId());
        boolean isAdmin = hostPrincipal.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isHost && !isAdmin) {
            throw new AccessDeniedException("You are not authorized to update this reservation.");
        }

        booking.setStatus(newStatus);
        if (newStatus == BookingStatus.CANCELLED) {
            booking.setPaymentStatus("REFUNDED");
        }
        Booking saved = bookingRepository.save(booking);
        return BookingResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public HostStatsDto getHostStats(Long hostId) {
        HostStatsDto stats = new HostStatsDto();
        stats.setTotalListings(propertyRepository.countByHostId(hostId));
        stats.setTotalReservations(bookingRepository.countByPropertyHostId(hostId));

        BigDecimal earnings = bookingRepository.sumEarningsByHostId(hostId);
        stats.setTotalEarnings(earnings != null ? earnings : BigDecimal.ZERO);

        List<Property> hostProperties = propertyRepository.findByHostIdOrderByCreatedAtDesc(hostId);
        double avg = hostProperties.stream()
                .mapToDouble(p -> p.getRating() != null ? p.getRating() : 0.0)
                .average()
                .orElse(0.0);
        stats.setAverageRating(BigDecimal.valueOf(avg).setScale(2, RoundingMode.HALF_UP).doubleValue());

        return stats;
    }

    private void populatePropertyDetails(Property property, PropertyRequest request) {
        property.setTitle(request.getTitle().trim());
        property.setDescription(request.getDescription().trim());
        property.setPropertyType(request.getPropertyType().trim());
        property.setAddress(request.getAddress().trim());
        property.setCity(request.getCity().trim());
        property.setState(request.getState() != null ? request.getState().trim() : null);
        property.setCountry(request.getCountry().trim());
        property.setZipCode(request.getZipCode() != null ? request.getZipCode().trim() : null);
        property.setPricePerNight(request.getPricePerNight());
        property.setCleaningFee(request.getCleaningFee() != null ? request.getCleaningFee() : BigDecimal.ZERO);
        property.setMaxGuests(request.getMaxGuests());
        property.setBedrooms(request.getBedrooms());
        property.setBeds(request.getBeds());
        property.setBathrooms(request.getBathrooms());

        if (request.getAmenityIds() != null && !request.getAmenityIds().isEmpty()) {
            List<Amenity> amenities = amenityRepository.findByIdIn(request.getAmenityIds());
            property.setAmenities(new HashSet<>(amenities));
        } else {
            property.setAmenities(new HashSet<>());
        }
    }

    private List<PropertyImage> savePropertyImages(Property property, List<String> imageUrls) {
        List<PropertyImage> savedList = new ArrayList<>();
        if (imageUrls == null || imageUrls.isEmpty()) {
            // Add default luxury photo
            PropertyImage defaultImg = new PropertyImage(property,
                    "https://images.unsplash.com/photo-1512917774080-9991f1c4c750?auto=format&fit=crop&w=1200&q=80",
                    "Main View", true, 1);
            savedList.add(propertyImageRepository.save(defaultImg));
            return savedList;
        }

        for (int i = 0; i < imageUrls.size(); i++) {
            String url = imageUrls.get(i).trim();
            if (!url.isEmpty()) {
                PropertyImage img = new PropertyImage(property, url, "Image " + (i + 1), i == 0, i + 1);
                savedList.add(propertyImageRepository.save(img));
            }
        }
        return savedList;
    }
}
