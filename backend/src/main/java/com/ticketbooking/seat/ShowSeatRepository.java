package com.ticketbooking.seat;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ShowSeatRepository extends JpaRepository<ShowSeat, Long> {

    List<ShowSeat> findByShowIdOrderBySeatRowNumberAscSeatSeatNumberAsc(Long showId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM ShowSeat s WHERE s.id IN :ids")
    List<ShowSeat> findAllByIdWithPessimisticLock(@Param("ids") List<Long> ids);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM ShowSeat s WHERE s.id = :id")
    Optional<ShowSeat> findByIdWithPessimisticLock(@Param("id") Long id);

    List<ShowSeat> findByStatusAndHoldExpiresAtBefore(SeatStatus status, LocalDateTime dateTime);

    long countByShowIdAndStatus(Long showId, SeatStatus status);

    long countByShowId(Long showId);

    @Query("SELECT s FROM ShowSeat s WHERE s.show.id = :showId AND s.seat.category.id = :categoryId AND s.status = 'AVAILABLE'")
    List<ShowSeat> findAvailableSeatsByCategory(@Param("showId") Long showId, @Param("categoryId") Long categoryId);

    @Query("SELECT COUNT(s) FROM ShowSeat s WHERE s.show.id = :showId AND s.seat.category.id = :categoryId AND s.status = 'AVAILABLE'")
    long countAvailableSeatsByCategory(@Param("showId") Long showId, @Param("categoryId") Long categoryId);
}
