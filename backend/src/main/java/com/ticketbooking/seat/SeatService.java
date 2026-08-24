package com.ticketbooking.seat;

import com.ticketbooking.common.exception.BadRequestException;
import com.ticketbooking.common.exception.ResourceNotFoundException;
import com.ticketbooking.common.exception.SeatUnavailableException;
import com.ticketbooking.event.Show;
import com.ticketbooking.event.ShowRepository;
import com.ticketbooking.seat.dto.HoldSeatsRequest;
import com.ticketbooking.seat.dto.HoldSeatsResponse;
import com.ticketbooking.seat.dto.ShowSeatResponse;
import com.ticketbooking.user.User;
import com.ticketbooking.user.UserService;
import com.ticketbooking.websocket.WebSocketNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SeatService {

    private final ShowSeatRepository showSeatRepository;
    private final ShowRepository showRepository;
    private final UserService userService;
    private final WebSocketNotificationService webSocketNotificationService;
    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${app.seat.hold-ttl-minutes:10}")
    private int holdTtlMinutes;

    public List<ShowSeatResponse> getSeatMap(Long showId) {
        if (!showRepository.existsById(showId)) {
            throw new ResourceNotFoundException("Show not found with id: " + showId);
        }

        User currentUser = null;
        try {
            currentUser = userService.getCurrentUser();
        } catch (Exception ignored) {
            // Unauthenticated users can view the seat map
        }

        final Long currentUserId = (currentUser != null) ? currentUser.getId() : null;

        return showSeatRepository.findByShowIdOrderBySeatRowNumberAscSeatSeatNumberAsc(showId).stream()
                .map(seat -> mapToShowSeatResponse(seat, currentUserId))
                .collect(Collectors.toList());
    }

    @Transactional
    public HoldSeatsResponse holdSeats(Long showId, HoldSeatsRequest request) {
        Show show = showRepository.findById(showId)
                .orElseThrow(() -> new ResourceNotFoundException("Show not found with id: " + showId));

        User currentUser = userService.getCurrentUser();
        List<Long> seatIds = request.getShowSeatIds();

        if (seatIds == null || seatIds.isEmpty()) {
            throw new BadRequestException("No seats specified for hold");
        }

        // Concurrency Protection: Acquire pessimistic write lock on selected seats
        List<ShowSeat> lockedSeats = showSeatRepository.findAllByIdWithPessimisticLock(seatIds);

        if (lockedSeats.size() != seatIds.size()) {
            throw new ResourceNotFoundException("One or more selected seats were not found");
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusMinutes(holdTtlMinutes);
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (ShowSeat seat : lockedSeats) {
            if (!seat.getShow().getId().equals(showId)) {
                throw new BadRequestException("Seat " + seat.getId() + " does not belong to show " + showId);
            }

            // Check if seat is already booked
            if (seat.getStatus() == SeatStatus.BOOKED) {
                throw new SeatUnavailableException("Seat " + seat.getSeat().getRowNumber() + seat.getSeat().getSeatNumber() + " is already booked.");
            }

            // Check if seat is currently held by someone else and not expired
            if (seat.getStatus() == SeatStatus.HELD) {
                boolean isHeldByAnother = (seat.getHeldBy() != null && !seat.getHeldBy().getId().equals(currentUser.getId()));
                boolean isExpired = (seat.getHoldExpiresAt() != null && seat.getHoldExpiresAt().isBefore(now));

                if (isHeldByAnother && !isExpired) {
                    throw new SeatUnavailableException("Seat " + seat.getSeat().getRowNumber() + seat.getSeat().getSeatNumber() + " is currently held by another user.");
                }
            }

            // Place or refresh hold
            seat.setStatus(SeatStatus.HELD);
            seat.setHeldBy(currentUser);
            seat.setHoldExpiresAt(expiresAt);
            totalAmount = totalAmount.add(seat.getPrice());

            // Write hold key to Redis with TTL
            try {
                String redisKey = "hold:show:" + showId + ":seat:" + seat.getId();
                redisTemplate.opsForValue().set(redisKey, currentUser.getId().toString(), Duration.ofMinutes(holdTtlMinutes));
            } catch (Exception e) {
                log.warn("Redis hold caching failed (DB lock is authoritative): {}", e.getMessage());
            }
        }

        List<ShowSeat> savedSeats = showSeatRepository.saveAll(lockedSeats);

        // Broadcast HELD status to all active WebSocket clients viewing this show
        webSocketNotificationService.broadcastSeatUpdates(showId, savedSeats);

        List<ShowSeatResponse> seatResponses = savedSeats.stream()
                .map(seat -> mapToShowSeatResponse(seat, currentUser.getId()))
                .collect(Collectors.toList());

        return HoldSeatsResponse.builder()
                .showId(showId)
                .heldSeats(seatResponses)
                .totalAmount(totalAmount)
                .expiresAt(expiresAt)
                .remainingSeconds(holdTtlMinutes * 60L)
                .build();
    }

    @Transactional
    public void releaseHold(Long showSeatId) {
        User currentUser = userService.getCurrentUser();
        ShowSeat seat = showSeatRepository.findByIdWithPessimisticLock(showSeatId)
                .orElseThrow(() -> new ResourceNotFoundException("Seat not found with id: " + showSeatId));

        if (seat.getStatus() == SeatStatus.HELD) {
            // Only the user holding the seat or an ADMIN can release it
            if (seat.getHeldBy() != null && (seat.getHeldBy().getId().equals(currentUser.getId()) || currentUser.getRole().name().equals("ADMIN"))) {
                seat.setStatus(SeatStatus.AVAILABLE);
                seat.setHeldBy(null);
                seat.setHoldExpiresAt(null);
                showSeatRepository.save(seat);

                try {
                    String redisKey = "hold:show:" + seat.getShow().getId() + ":seat:" + seat.getId();
                    redisTemplate.delete(redisKey);
                } catch (Exception ignored) {}

                webSocketNotificationService.broadcastSingleSeatUpdate(seat.getShow().getId(), seat);
                log.info("Seat {} released by user {}", showSeatId, currentUser.getId());
            }
        }
    }

    public ShowSeatResponse mapToShowSeatResponse(ShowSeat seat, Long currentUserId) {
        boolean isHeldByMe = false;
        if (seat.getStatus() == SeatStatus.HELD && seat.getHeldBy() != null && currentUserId != null) {
            isHeldByMe = seat.getHeldBy().getId().equals(currentUserId);
        }

        return ShowSeatResponse.builder()
                .id(seat.getId())
                .seatId(seat.getSeat().getId())
                .rowNumber(seat.getSeat().getRowNumber())
                .seatNumber(seat.getSeat().getSeatNumber())
                .categoryId(seat.getSeat().getCategory().getId())
                .categoryName(seat.getSeat().getCategory().getName())
                .price(seat.getPrice())
                .status(seat.getStatus())
                .heldByUserId(seat.getHeldBy() != null ? seat.getHeldBy().getId() : null)
                .holdExpiresAt(seat.getHoldExpiresAt())
                .isHeldByMe(isHeldByMe)
                .build();
    }
}
