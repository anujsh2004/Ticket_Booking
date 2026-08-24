package com.ticketbooking.booking;

import com.ticketbooking.booking.dto.BookingResponse;
import com.ticketbooking.booking.dto.BookingSeatResponse;
import com.ticketbooking.booking.dto.CreateBookingRequest;
import com.ticketbooking.booking.dto.OrganiserDashboardStatsResponse;
import com.ticketbooking.common.exception.BadRequestException;
import com.ticketbooking.common.exception.ResourceNotFoundException;
import com.ticketbooking.common.exception.SeatUnavailableException;
import com.ticketbooking.event.EventRepository;
import com.ticketbooking.event.Show;
import com.ticketbooking.event.ShowRepository;
import com.ticketbooking.notification.EmailService;
import com.ticketbooking.notification.QrCodeService;
import com.ticketbooking.seat.SeatStatus;
import com.ticketbooking.seat.ShowSeat;
import com.ticketbooking.seat.ShowSeatRepository;
import com.ticketbooking.user.User;
import com.ticketbooking.user.UserService;
import com.ticketbooking.waitlist.WaitlistEntry;
import com.ticketbooking.waitlist.WaitlistEntryRepository;
import com.ticketbooking.waitlist.WaitlistOffer;
import com.ticketbooking.waitlist.WaitlistOfferRepository;
import com.ticketbooking.waitlist.WaitlistOfferStatus;
import com.ticketbooking.waitlist.WaitlistService;
import com.ticketbooking.waitlist.WaitlistStatus;
import com.ticketbooking.websocket.WebSocketNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingService {

    private final BookingRepository bookingRepository;
    private final BookingSeatRepository bookingSeatRepository;
    private final ShowSeatRepository showSeatRepository;
    private final ShowRepository showRepository;
    private final EventRepository eventRepository;
    private final WaitlistOfferRepository waitlistOfferRepository;
    private final WaitlistEntryRepository waitlistEntryRepository;
    private final UserService userService;
    private final WaitlistService waitlistService;
    private final QrCodeService qrCodeService;
    private final EmailService emailService;
    private final WebSocketNotificationService webSocketNotificationService;
    private final RedisTemplate<String, Object> redisTemplate;

    @Transactional
    public BookingResponse createBooking(CreateBookingRequest request) {
        User currentUser = userService.getCurrentUser();
        Show show = showRepository.findById(request.getShowId())
                .orElseThrow(() -> new ResourceNotFoundException("Show not found with id: " + request.getShowId()));

        List<Long> seatIds = request.getShowSeatIds();
        if (seatIds == null || seatIds.isEmpty()) {
            throw new BadRequestException("No seats selected for booking");
        }

        // Concurrency Protection: Lock seats for booking update
        List<ShowSeat> lockedSeats = showSeatRepository.findAllByIdWithPessimisticLock(seatIds);
        if (lockedSeats.size() != seatIds.size()) {
            throw new ResourceNotFoundException("One or more selected seats could not be found");
        }

        LocalDateTime now = LocalDateTime.now();
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<String> seatLabels = new ArrayList<>();

        for (ShowSeat seat : lockedSeats) {
            if (!seat.getShow().getId().equals(show.getId())) {
                throw new BadRequestException("Seat does not belong to show " + show.getId());
            }

            if (seat.getStatus() == SeatStatus.BOOKED) {
                throw new SeatUnavailableException("Seat " + seat.getSeat().getRowNumber() + seat.getSeat().getSeatNumber() + " is already booked.");
            }

            // Verify hold is active and belongs to this user
            boolean isHeldByMe = (seat.getStatus() == SeatStatus.HELD && seat.getHeldBy() != null && seat.getHeldBy().getId().equals(currentUser.getId()));
            boolean isExpired = (seat.getHoldExpiresAt() != null && seat.getHoldExpiresAt().isBefore(now));

            if (!isHeldByMe || isExpired) {
                throw new SeatUnavailableException("Hold expired or invalid for seat: " + seat.getSeat().getRowNumber() + seat.getSeat().getSeatNumber());
            }

            // Mark seat as BOOKED
            seat.setStatus(SeatStatus.BOOKED);
            seat.setHeldBy(null);
            seat.setHoldExpiresAt(null);
            totalAmount = totalAmount.add(seat.getPrice());
            seatLabels.add(seat.getSeat().getRowNumber() + seat.getSeat().getSeatNumber());

            // Remove Redis hold key
            try {
                String redisKey = "hold:show:" + show.getId() + ":seat:" + seat.getId();
                redisTemplate.delete(redisKey);
            } catch (Exception ignored) {}
        }

        showSeatRepository.saveAll(lockedSeats);

        // Generate Booking Reference (e.g. BK-2026-A1B2C3)
        String bookingReference = "BK-2026-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();

        // Generate QR code encoding booking reference
        String qrCodeBase64 = qrCodeService.generateQrCodeBase64(bookingReference, 300, 300);

        Booking booking = Booking.builder()
                .bookingReference(bookingReference)
                .user(currentUser)
                .show(show)
                .totalAmount(totalAmount)
                .status(BookingStatus.CONFIRMED)
                .qrCodeBase64(qrCodeBase64)
                .build();

        booking = bookingRepository.save(booking);

        // Create BookingSeat records
        List<BookingSeat> bookingSeats = new ArrayList<>();
        for (ShowSeat seat : lockedSeats) {
            BookingSeat bookingSeat = BookingSeat.builder()
                    .booking(booking)
                    .showSeat(seat)
                    .price(seat.getPrice())
                    .build();
            bookingSeats.add(bookingSeat);
        }
        bookingSeatRepository.saveAll(bookingSeats);
        booking.setBookingSeats(bookingSeats);

        // If this booking completed a waitlist offer
        if (request.getWaitlistOfferId() != null) {
            waitlistOfferRepository.findById(request.getWaitlistOfferId()).ifPresent(offer -> {
                offer.setStatus(WaitlistOfferStatus.ACCEPTED);
                waitlistOfferRepository.save(offer);
                WaitlistEntry entry = offer.getWaitlistEntry();
                entry.setStatus(WaitlistStatus.COMPLETED);
                waitlistEntryRepository.save(entry);
                log.info("Waitlist entry {} successfully converted to booking {}", entry.getId(), bookingReference);
            });
        }

        // Broadcast real-time BOOKED state to all clients
        webSocketNotificationService.broadcastSeatUpdates(show.getId(), lockedSeats);

        // Send confirmation email asynchronously
        String formattedDate = show.getStartTime().format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a"));
        emailService.sendBookingConfirmationEmail(
                currentUser.getEmail(),
                currentUser.getName(),
                bookingReference,
                show.getEvent().getTitle(),
                show.getVenue().getName(),
                formattedDate,
                seatLabels,
                totalAmount
        );

        return mapToBookingResponse(booking);
    }

    @Transactional
    public BookingResponse cancelBooking(Long bookingId) {
        User currentUser = userService.getCurrentUser();
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + bookingId));

        if (!booking.getUser().getId().equals(currentUser.getId()) && !currentUser.getRole().name().equals("ADMIN")) {
            throw new BadRequestException("You are not authorized to cancel this booking");
        }

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new BadRequestException("Booking is already cancelled");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        booking.setCancelledAt(LocalDateTime.now());
        booking = bookingRepository.save(booking);

        List<BookingSeat> bookingSeats = bookingSeatRepository.findByBookingId(booking.getId());
        for (BookingSeat bs : bookingSeats) {
            ShowSeat seat = bs.getShowSeat();
            // Free the seat and trigger intelligent waitlist allocation
            waitlistService.processAvailableSeatForWaitlist(seat);
        }

        log.info("Booking {} successfully cancelled. Seats reassigned to waitlist or made available.", booking.getBookingReference());
        return mapToBookingResponse(booking);
    }

    public List<BookingResponse> getMyBookings() {
        User currentUser = userService.getCurrentUser();
        return bookingRepository.findByUserIdOrderByCreatedAtDesc(currentUser.getId()).stream()
                .map(this::mapToBookingResponse)
                .collect(Collectors.toList());
    }

    public BookingResponse getBookingById(Long bookingId) {
        User currentUser = userService.getCurrentUser();
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + bookingId));

        if (!booking.getUser().getId().equals(currentUser.getId()) && !currentUser.getRole().name().equals("ADMIN") && !currentUser.getRole().name().equals("ORGANISER")) {
            throw new BadRequestException("You are not authorized to view this booking");
        }

        return mapToBookingResponse(booking);
    }

    public OrganiserDashboardStatsResponse getOrganiserStats() {
        User organiser = userService.getCurrentUser();
        BigDecimal totalRevenue = bookingRepository.calculateTotalRevenueByOrganiser(organiser.getId());
        long totalConfirmed = bookingRepository.countByShowEventOrganiserIdAndStatus(organiser.getId(), BookingStatus.CONFIRMED);
        long totalEvents = eventRepository.findByOrganiserId(organiser.getId()).size();
        List<Booking> recentBookings = bookingRepository.findByOrganiserId(organiser.getId());

        List<BookingResponse> recentResponses = recentBookings.stream()
                .limit(10)
                .map(this::mapToBookingResponse)
                .collect(Collectors.toList());

        return OrganiserDashboardStatsResponse.builder()
                .totalRevenue(totalRevenue)
                .totalTicketsSold(totalConfirmed)
                .totalEvents(totalEvents)
                .activeShows(recentBookings.stream().map(b -> b.getShow().getId()).distinct().count())
                .recentBookings(recentResponses)
                .build();
    }

    public BookingResponse mapToBookingResponse(Booking booking) {
        List<BookingSeatResponse> seatResponses = bookingSeatRepository.findByBookingId(booking.getId()).stream()
                .map(bs -> BookingSeatResponse.builder()
                        .id(bs.getId())
                        .showSeatId(bs.getShowSeat().getId())
                        .rowNumber(bs.getShowSeat().getSeat().getRowNumber())
                        .seatNumber(bs.getShowSeat().getSeat().getSeatNumber())
                        .categoryName(bs.getShowSeat().getSeat().getCategory().getName())
                        .price(bs.getPrice())
                        .build())
                .collect(Collectors.toList());

        return BookingResponse.builder()
                .id(booking.getId())
                .bookingReference(booking.getBookingReference())
                .userId(booking.getUser().getId())
                .userName(booking.getUser().getName())
                .userEmail(booking.getUser().getEmail())
                .showId(booking.getShow().getId())
                .eventTitle(booking.getShow().getEvent().getTitle())
                .venueName(booking.getShow().getVenue().getName())
                .venueLocation(booking.getShow().getVenue().getLocation())
                .showStartTime(booking.getShow().getStartTime())
                .totalAmount(booking.getTotalAmount())
                .status(booking.getStatus())
                .qrCodeBase64(booking.getQrCodeBase64())
                .createdAt(booking.getCreatedAt())
                .cancelledAt(booking.getCancelledAt())
                .seats(seatResponses)
                .build();
    }
}
