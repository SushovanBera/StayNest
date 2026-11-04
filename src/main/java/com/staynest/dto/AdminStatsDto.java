package com.staynest.dto;

import java.math.BigDecimal;

public class AdminStatsDto {
    private long totalUsers;
    private long totalHosts;
    private long totalGuests;
    private long totalProperties;
    private long activeProperties;
    private long totalBookings;
    private long confirmedBookings;
    private BigDecimal totalRevenue;

    public AdminStatsDto() {}

    public long getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(long totalUsers) {
        this.totalUsers = totalUsers;
    }

    public long getTotalHosts() {
        return totalHosts;
    }

    public void setTotalHosts(long totalHosts) {
        this.totalHosts = totalHosts;
    }

    public long getTotalGuests() {
        return totalGuests;
    }

    public void setTotalGuests(long totalGuests) {
        this.totalGuests = totalGuests;
    }

    public long getTotalProperties() {
        return totalProperties;
    }

    public void setTotalProperties(long totalProperties) {
        this.totalProperties = totalProperties;
    }

    public long getActiveProperties() {
        return activeProperties;
    }

    public void setActiveProperties(long activeProperties) {
        this.activeProperties = activeProperties;
    }

    public long getTotalBookings() {
        return totalBookings;
    }

    public void setTotalBookings(long totalBookings) {
        this.totalBookings = totalBookings;
    }

    public long getConfirmedBookings() {
        return confirmedBookings;
    }

    public void setConfirmedBookings(long confirmedBookings) {
        this.confirmedBookings = confirmedBookings;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }
}
