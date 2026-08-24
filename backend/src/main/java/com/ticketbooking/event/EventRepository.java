package com.ticketbooking.event;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByOrganiserId(Long organiserId);
    List<Event> findByEventType(EventType eventType);
    List<Event> findByTitleContainingIgnoreCase(String query);
}
