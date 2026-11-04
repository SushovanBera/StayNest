package com.staynest.repository;

import com.staynest.entity.Property;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface PropertyRepository extends JpaRepository<Property, Long> {

    List<Property> findByActiveTrueOrderByCreatedAtDesc();

    List<Property> findByFeaturedTrueAndActiveTrue();

    List<Property> findByCategoryIdAndActiveTrue(Long categoryId);

    List<Property> findByHostIdOrderByCreatedAtDesc(Long hostId);

    long countByHostId(Long hostId);

    long countByActiveTrue();

    @Query("SELECT DISTINCT p FROM Property p " +
           "WHERE p.active = true " +
           "AND (:query IS NULL OR :query = '' " +
           "     OR LOWER(p.title) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "     OR LOWER(p.city) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "     OR LOWER(p.state) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "     OR LOWER(p.country) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "     OR LOWER(p.address) LIKE LOWER(CONCAT('%', :query, '%'))) " +
           "AND (:categorySlug IS NULL OR :categorySlug = '' OR p.category.slug = :categorySlug) " +
           "AND (:propertyType IS NULL OR :propertyType = '' OR p.propertyType = :propertyType) " +
           "AND (:minPrice IS NULL OR p.pricePerNight >= :minPrice) " +
           "AND (:maxPrice IS NULL OR p.pricePerNight <= :maxPrice) " +
           "AND (:guests IS NULL OR p.maxGuests >= :guests) " +
           "AND (:minRating IS NULL OR p.rating >= :minRating) " +
           "AND (:checkIn IS NULL OR :checkOut IS NULL OR p.id NOT IN (" +
           "     SELECT b.property.id FROM Booking b " +
           "     WHERE b.status != com.staynest.entity.BookingStatus.CANCELLED " +
           "     AND b.checkIn < :checkOut AND b.checkOut > :checkIn" +
           "))")
    List<Property> searchProperties(
            @Param("query") String query,
            @Param("categorySlug") String categorySlug,
            @Param("propertyType") String propertyType,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("guests") Integer guests,
            @Param("minRating") Double minRating,
            @Param("checkIn") LocalDate checkIn,
            @Param("checkOut") LocalDate checkOut);
}
