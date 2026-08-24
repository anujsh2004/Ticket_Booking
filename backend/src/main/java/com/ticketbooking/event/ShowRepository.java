package com.ticketbooking.event;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShowRepository extends JpaRepository<Show, Long> {
    List<Show> findByEventIdOrderByStartTimeAsc(Long eventId);
    List<Show> findByVenueId(Long venueId);
}
