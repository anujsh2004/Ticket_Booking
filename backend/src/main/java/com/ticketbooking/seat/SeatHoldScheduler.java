package com.ticketbooking.seat;

import com.ticketbooking.waitlist.WaitlistService;
import com.ticketbooking.websocket.WebSocketNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class SeatHoldScheduler {

    private final ShowSeatRepository showSeatRepository;
    private final WaitlistService waitlistService;
    private final WebSocketNotificationService webSocketNotificationService;
    private final RedisTemplate<String, Object> redisTemplate;

    @Scheduled(fixedRate = 5000) // Runs every 5 seconds
    @Transactional
    public void cleanupExpiredSeatHoldsAndOffers() {
        LocalDateTime now = LocalDateTime.now();

        // 1. Process expired seat holds
        List<ShowSeat> expiredHolds = showSeatRepository.findByStatusAndHoldExpiresAtBefore(SeatStatus.HELD, now);
        for (ShowSeat seat : expiredHolds) {
            log.info("Hold expired for seat {} (Show: {})", seat.getId(), seat.getShow().getId());

            try {
                String redisKey = "hold:show:" + seat.getShow().getId() + ":seat:" + seat.getId();
                redisTemplate.delete(redisKey);
            } catch (Exception ignored) {}

            // Try to assign seat to waitlist, otherwise revert to AVAILABLE
            waitlistService.processAvailableSeatForWaitlist(seat);
        }

        // 2. Expire unaccepted waitlist offers and reassign to next in queue
        waitlistService.expirePendingOffersAndReassign();
    }
}
