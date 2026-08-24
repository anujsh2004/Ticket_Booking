package com.ticketbooking.venue;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {
    List<Seat> findByVenueIdOrderByRowNumberAscSeatNumberAsc(Long venueId);
    boolean existsByVenueIdAndRowNumberAndSeatNumber(Long venueId, String rowNumber, Integer seatNumber);
}
