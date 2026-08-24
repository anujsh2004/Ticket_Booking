package com.ticketbooking.waitlist;

import com.ticketbooking.common.exception.BadRequestException;
import com.ticketbooking.common.exception.ResourceNotFoundException;
import com.ticketbooking.event.Show;
import com.ticketbooking.event.ShowRepository;
import com.ticketbooking.notification.EmailService;
import com.ticketbooking.seat.SeatStatus;
import com.ticketbooking.seat.ShowSeat;
import com.ticketbooking.seat.ShowSeatRepository;
import com.ticketbooking.user.User;
import com.ticketbooking.user.UserService;
import com.ticketbooking.venue.SeatCategory;
import com.ticketbooking.venue.SeatCategoryRepository;
import com.ticketbooking.waitlist.dto.JoinWaitlistRequest;
import com.ticketbooking.waitlist.dto.WaitlistOfferResponse;
import com.ticketbooking.waitlist.dto.WaitlistResponse;
import com.ticketbooking.websocket.WebSocketNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class WaitlistService {

    private final WaitlistEntryRepository waitlistEntryRepository;
    private final WaitlistOfferRepository waitlistOfferRepository;
    private final ShowSeatRepository showSeatRepository;
    private final ShowRepository showRepository;
    private final SeatCategoryRepository seatCategoryRepository;
    private final UserService userService;
    private final EmailService emailService;
    private final WebSocketNotificationService webSocketNotificationService;

    @Value("${app.waitlist.offer-ttl-minutes:15}")
    private int offerTtlMinutes;

    @Transactional
    public WaitlistResponse joinWaitlist(Long showId, JoinWaitlistRequest request) {
        Show show = showRepository.findById(showId)
                .orElseThrow(() -> new ResourceNotFoundException("Show not found with id: " + showId));

        SeatCategory category = seatCategoryRepository.findById(request.getSeatCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Seat category not found with id: " + request.getSeatCategoryId()));

        User user = userService.getCurrentUser();

        // Check if user is already waiting/offered for this show & category
        boolean alreadyInQueue = waitlistEntryRepository.existsByShowIdAndUserIdAndSeatCategoryIdAndStatusIn(
                showId, user.getId(), category.getId(), List.of(WaitlistStatus.WAITING, WaitlistStatus.OFFERED));
        if (alreadyInQueue) {
            throw new BadRequestException("You are already on the waitlist for this show and category.");
        }

        int maxPosition = waitlistEntryRepository.findMaxPositionByShowAndCategory(showId, category.getId());
        int newPosition = maxPosition + 1;

        WaitlistEntry entry = WaitlistEntry.builder()
                .show(show)
                .user(user)
                .seatCategory(category)
                .position(newPosition)
                .status(WaitlistStatus.WAITING)
                .build();

        entry = waitlistEntryRepository.save(entry);
        log.info("User {} joined waitlist for show {} in category {} at position {}", user.getId(), showId, category.getName(), newPosition);

        return mapToWaitlistResponse(entry);
    }

    public List<WaitlistResponse> getMyWaitlistEntries() {
        User user = userService.getCurrentUser();
        return waitlistEntryRepository.findByUserIdOrderByCreatedAtDesc(user.getId()).stream()
                .map(this::mapToWaitlistResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public boolean processAvailableSeatForWaitlist(ShowSeat seat) {
        Long showId = seat.getShow().getId();
        Long categoryId = seat.getSeat().getCategory().getId();

        // Find the next customer in line (FIFO)
        Optional<WaitlistEntry> nextWaitingOpt = waitlistEntryRepository
                .findFirstByShowIdAndSeatCategoryIdAndStatusOrderByPositionAsc(
                        showId, categoryId, WaitlistStatus.WAITING);

        if (nextWaitingOpt.isEmpty()) {
            // No one on waitlist: release seat to general availability
            seat.setStatus(SeatStatus.AVAILABLE);
            seat.setHeldBy(null);
            seat.setHoldExpiresAt(null);
            showSeatRepository.save(seat);
            webSocketNotificationService.broadcastSingleSeatUpdate(showId, seat);
            log.info("Seat {} released to AVAILABLE (no active waitlist).", seat.getId());
            return false;
        }

        WaitlistEntry entry = nextWaitingOpt.get();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(offerTtlMinutes);

        // Lock the seat as HELD for the waitlisted user
        seat.setStatus(SeatStatus.HELD);
        seat.setHeldBy(entry.getUser());
        seat.setHoldExpiresAt(expiresAt);
        showSeatRepository.save(seat);

        // Create the time-limited waitlist offer
        WaitlistOffer offer = WaitlistOffer.builder()
                .waitlistEntry(entry)
                .showSeat(seat)
                .expiresAt(expiresAt)
                .status(WaitlistOfferStatus.PENDING)
                .build();
        waitlistOfferRepository.save(offer);

        // Update waitlist entry status
        entry.setStatus(WaitlistStatus.OFFERED);
        waitlistEntryRepository.save(entry);

        // Notify client via WebSocket and Email
        webSocketNotificationService.broadcastSingleSeatUpdate(showId, seat);
        emailService.sendWaitlistOfferEmail(
                entry.getUser().getEmail(),
                entry.getUser().getName(),
                seat.getShow().getEvent().getTitle(),
                seat.getSeat().getRowNumber() + seat.getSeat().getSeatNumber(),
                seat.getSeat().getCategory().getName(),
                offer.getId(),
                offerTtlMinutes
        );

        log.info("Assigned seat {} as offer {} to waitlist user {}", seat.getId(), offer.getId(), entry.getUser().getEmail());
        return true;
    }

    @Transactional
    public void expirePendingOffersAndReassign() {
        LocalDateTime now = LocalDateTime.now();
        List<WaitlistOffer> expiredOffers = waitlistOfferRepository.findByStatusAndExpiresAtBefore(
                WaitlistOfferStatus.PENDING, now);

        for (WaitlistOffer offer : expiredOffers) {
            log.info("Expiring waitlist offer {} for entry {}", offer.getId(), offer.getWaitlistEntry().getId());
            offer.setStatus(WaitlistOfferStatus.EXPIRED);
            waitlistOfferRepository.save(offer);

            WaitlistEntry entry = offer.getWaitlistEntry();
            entry.setStatus(WaitlistStatus.EXPIRED);
            waitlistEntryRepository.save(entry);

            // Reassign seat to the next waitlist user
            ShowSeat seat = offer.getShowSeat();
            processAvailableSeatForWaitlist(seat);
        }
    }

    public WaitlistOffer getOfferById(Long offerId) {
        return waitlistOfferRepository.findById(offerId)
                .orElseThrow(() -> new ResourceNotFoundException("Waitlist offer not found with id: " + offerId));
    }

    public WaitlistResponse mapToWaitlistResponse(WaitlistEntry entry) {
        WaitlistOfferResponse activeOffer = waitlistOfferRepository
                .findByWaitlistEntryIdAndStatus(entry.getId(), WaitlistOfferStatus.PENDING)
                .map(this::mapToOfferResponse)
                .orElse(null);

        return WaitlistResponse.builder()
                .id(entry.getId())
                .showId(entry.getShow().getId())
                .eventTitle(entry.getShow().getEvent().getTitle())
                .venueName(entry.getShow().getVenue().getName())
                .showStartTime(entry.getShow().getStartTime())
                .seatCategoryId(entry.getSeatCategory().getId())
                .seatCategoryName(entry.getSeatCategory().getName())
                .position(entry.getPosition())
                .status(entry.getStatus())
                .createdAt(entry.getCreatedAt())
                .activeOffer(activeOffer)
                .build();
    }

    public WaitlistOfferResponse mapToOfferResponse(WaitlistOffer offer) {
        long remaining = Math.max(0, Duration.between(LocalDateTime.now(), offer.getExpiresAt()).getSeconds());
        return WaitlistOfferResponse.builder()
                .id(offer.getId())
                .waitlistEntryId(offer.getWaitlistEntry().getId())
                .showSeatId(offer.getShowSeat().getId())
                .seatRow(offer.getShowSeat().getSeat().getRowNumber())
                .seatNumber(offer.getShowSeat().getSeat().getSeatNumber())
                .categoryName(offer.getShowSeat().getSeat().getCategory().getName())
                .price(offer.getShowSeat().getPrice())
                .expiresAt(offer.getExpiresAt())
                .remainingSeconds(remaining)
                .status(offer.getStatus())
                .build();
    }
}
