package com.ticketbooking.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<Booking> findByBookingReference(String bookingReference);

    List<Booking> findByShowId(Long showId);

    @Query("SELECT b FROM Booking b WHERE b.show.event.id = :eventId ORDER BY b.createdAt DESC")
    List<Booking> findByEventId(@Param("eventId") Long eventId);

    @Query("SELECT b FROM Booking b WHERE b.show.event.organiser.id = :organiserId ORDER BY b.createdAt DESC")
    List<Booking> findByOrganiserId(@Param("organiserId") Long organiserId);

    @Query("SELECT COALESCE(SUM(b.totalAmount), 0) FROM Booking b WHERE b.show.event.organiser.id = :organiserId AND b.status = 'CONFIRMED'")
    BigDecimal calculateTotalRevenueByOrganiser(@Param("organiserId") Long organiserId);

    @Query("SELECT COALESCE(SUM(b.totalAmount), 0) FROM Booking b WHERE b.show.event.id = :eventId AND b.status = 'CONFIRMED'")
    BigDecimal calculateTotalRevenueByEvent(@Param("eventId") Long eventId);

    long countByShowEventOrganiserIdAndStatus(Long organiserId, BookingStatus status);
}
