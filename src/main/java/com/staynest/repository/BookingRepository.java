package com.staynest.repository;

import com.staynest.entity.Booking;
import com.staynest.entity.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    Optional<Booking> findByBookingCode(String bookingCode);

    List<Booking> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<Booking> findByPropertyHostIdOrderByCreatedAtDesc(Long hostId);

    List<Booking> findByPropertyIdOrderByCheckInAsc(Long propertyId);

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.property.id = :propertyId " +
           "AND b.status != com.staynest.entity.BookingStatus.CANCELLED " +
           "AND b.checkIn < :checkOut AND b.checkOut > :checkIn")
    long countConflictingBookings(@Param("propertyId") Long propertyId,
                                  @Param("checkIn") LocalDate checkIn,
                                  @Param("checkOut") LocalDate checkOut);

    @Query("SELECT b FROM Booking b WHERE b.property.id = :propertyId " +
           "AND b.status != com.staynest.entity.BookingStatus.CANCELLED " +
           "AND b.checkIn < :checkOut AND b.checkOut > :checkIn")
    List<Booking> findConflictingBookings(@Param("propertyId") Long propertyId,
                                         @Param("checkIn") LocalDate checkIn,
                                         @Param("checkOut") LocalDate checkOut);

    @Query("SELECT COALESCE(SUM(b.totalPrice), 0) FROM Booking b WHERE b.property.host.id = :hostId AND b.status != com.staynest.entity.BookingStatus.CANCELLED")
    BigDecimal sumEarningsByHostId(@Param("hostId") Long hostId);

    @Query("SELECT COALESCE(SUM(b.totalPrice), 0) FROM Booking b WHERE b.status != com.staynest.entity.BookingStatus.CANCELLED")
    BigDecimal sumTotalPlatformRevenue();

    long countByStatus(BookingStatus status);

    long countByPropertyHostId(Long hostId);
}
