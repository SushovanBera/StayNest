package com.staynest.dto;

import com.staynest.entity.Booking;
import com.staynest.entity.BookingStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class BookingResponse {
    private Long id;
    private String bookingCode;
    private Long propertyId;
    private String propertyTitle;
    private String propertyImage;
    private String propertyCity;
    private String propertyCountry;
    private String propertyAddress;
    private String propertyType;
    private Long hostId;
    private String hostName;
    private String hostEmail;
    private Long userId;
    private String guestName;
    private String guestEmail;
    private LocalDate checkIn;
    private LocalDate checkOut;
    private Integer guestsCount;
    private Integer nightsCount;
    private BigDecimal basePrice;
    private BigDecimal cleaningFee;
    private BigDecimal serviceFee;
    private BigDecimal taxesAmount;
    private BigDecimal totalPrice;
    private BookingStatus status;
    private String paymentStatus;
    private String paymentMethod;
    private String specialRequests;
    private LocalDateTime createdAt;

    public BookingResponse() {}

    public static BookingResponse fromEntity(Booking b) {
        if (b == null) return null;

        BookingResponse dto = new BookingResponse();
        dto.setId(b.getId());
        dto.setBookingCode(b.getBookingCode());
        if (b.getProperty() != null) {
            dto.setPropertyId(b.getProperty().getId());
            dto.setPropertyTitle(b.getProperty().getTitle());
            dto.setPropertyImage(b.getProperty().getPrimaryImageUrl());
            dto.setPropertyCity(b.getProperty().getCity());
            dto.setPropertyCountry(b.getProperty().getCountry());
            dto.setPropertyAddress(b.getProperty().getAddress());
            dto.setPropertyType(b.getProperty().getPropertyType());
            if (b.getProperty().getHost() != null) {
                dto.setHostId(b.getProperty().getHost().getId());
                dto.setHostName(b.getProperty().getHost().getFullName());
                dto.setHostEmail(b.getProperty().getHost().getEmail());
            }
        }
        if (b.getUser() != null) {
            dto.setUserId(b.getUser().getId());
            dto.setGuestName(b.getUser().getFullName());
            dto.setGuestEmail(b.getUser().getEmail());
        }
        dto.setCheckIn(b.getCheckIn());
        dto.setCheckOut(b.getCheckOut());
        dto.setGuestsCount(b.getGuestsCount());
        dto.setNightsCount(b.getNightsCount());
        dto.setBasePrice(b.getBasePrice());
        dto.setCleaningFee(b.getCleaningFee());
        dto.setServiceFee(b.getServiceFee());
        dto.setTaxesAmount(b.getTaxesAmount());
        dto.setTotalPrice(b.getTotalPrice());
        dto.setStatus(b.getStatus());
        dto.setPaymentStatus(b.getPaymentStatus());
        dto.setPaymentMethod(b.getPaymentMethod());
        dto.setSpecialRequests(b.getSpecialRequests());
        dto.setCreatedAt(b.getCreatedAt());

        return dto;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBookingCode() {
        return bookingCode;
    }

    public void setBookingCode(String bookingCode) {
        this.bookingCode = bookingCode;
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

    public String getPropertyImage() {
        return propertyImage;
    }

    public void setPropertyImage(String propertyImage) {
        this.propertyImage = propertyImage;
    }

    public String getPropertyCity() {
        return propertyCity;
    }

    public void setPropertyCity(String propertyCity) {
        this.propertyCity = propertyCity;
    }

    public String getPropertyCountry() {
        return propertyCountry;
    }

    public void setPropertyCountry(String propertyCountry) {
        this.propertyCountry = propertyCountry;
    }

    public String getPropertyAddress() {
        return propertyAddress;
    }

    public void setPropertyAddress(String propertyAddress) {
        this.propertyAddress = propertyAddress;
    }

    public String getPropertyType() {
        return propertyType;
    }

    public void setPropertyType(String propertyType) {
        this.propertyType = propertyType;
    }

    public Long getHostId() {
        return hostId;
    }

    public void setHostId(Long hostId) {
        this.hostId = hostId;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getHostEmail() {
        return hostEmail;
    }

    public void setHostEmail(String hostEmail) {
        this.hostEmail = hostEmail;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getGuestName() {
        return guestName;
    }

    public void setGuestName(String guestName) {
        this.guestName = guestName;
    }

    public String getGuestEmail() {
        return guestEmail;
    }

    public void setGuestEmail(String guestEmail) {
        this.guestEmail = guestEmail;
    }

    public LocalDate getCheckIn() {
        return checkIn;
    }

    public void setCheckIn(LocalDate checkIn) {
        this.checkIn = checkIn;
    }

    public LocalDate getCheckOut() {
        return checkOut;
    }

    public void setCheckOut(LocalDate checkOut) {
        this.checkOut = checkOut;
    }

    public Integer getGuestsCount() {
        return guestsCount;
    }

    public void setGuestsCount(Integer guestsCount) {
        this.guestsCount = guestsCount;
    }

    public Integer getNightsCount() {
        return nightsCount;
    }

    public void setNightsCount(Integer nightsCount) {
        this.nightsCount = nightsCount;
    }

    public BigDecimal getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(BigDecimal basePrice) {
        this.basePrice = basePrice;
    }

    public BigDecimal getCleaningFee() {
        return cleaningFee;
    }

    public void setCleaningFee(BigDecimal cleaningFee) {
        this.cleaningFee = cleaningFee;
    }

    public BigDecimal getServiceFee() {
        return serviceFee;
    }

    public void setServiceFee(BigDecimal serviceFee) {
        this.serviceFee = serviceFee;
    }

    public BigDecimal getTaxesAmount() {
        return taxesAmount;
    }

    public void setTaxesAmount(BigDecimal taxesAmount) {
        this.taxesAmount = taxesAmount;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getSpecialRequests() {
        return specialRequests;
    }

    public void setSpecialRequests(String specialRequests) {
        this.specialRequests = specialRequests;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
