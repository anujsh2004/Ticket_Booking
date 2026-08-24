package com.ticketbooking.venue;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SeatCategoryRepository extends JpaRepository<SeatCategory, Long> {
    Optional<SeatCategory> findByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCase(String name);
}
