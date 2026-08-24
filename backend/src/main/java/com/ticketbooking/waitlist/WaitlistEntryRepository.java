package com.ticketbooking.waitlist;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WaitlistEntryRepository extends JpaRepository<WaitlistEntry, Long> {

    List<WaitlistEntry> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<WaitlistEntry> findByShowIdAndSeatCategoryIdAndStatusOrderByPositionAsc(
            Long showId, Long seatCategoryId, WaitlistStatus status);

    Optional<WaitlistEntry> findFirstByShowIdAndSeatCategoryIdAndStatusOrderByPositionAsc(
            Long showId, Long seatCategoryId, WaitlistStatus status);

    boolean existsByShowIdAndUserIdAndSeatCategoryIdAndStatusIn(
            Long showId, Long userId, Long seatCategoryId, List<WaitlistStatus> statuses);

    @Query("SELECT COALESCE(MAX(w.position), 0) FROM WaitlistEntry w WHERE w.show.id = :showId AND w.seatCategory.id = :categoryId")
    int findMaxPositionByShowAndCategory(@Param("showId") Long showId, @Param("categoryId") Long categoryId);

    long countByShowIdAndSeatCategoryIdAndStatus(Long showId, Long seatCategoryId, WaitlistStatus status);
}
