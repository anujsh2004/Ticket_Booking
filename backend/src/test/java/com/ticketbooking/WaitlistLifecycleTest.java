package com.ticketbooking;

import com.ticketbooking.booking.BookingService;
import com.ticketbooking.booking.BookingStatus;
import com.ticketbooking.booking.dto.BookingResponse;
import com.ticketbooking.booking.dto.CreateBookingRequest;
import com.ticketbooking.event.Event;
import com.ticketbooking.event.EventRepository;
import com.ticketbooking.event.EventType;
import com.ticketbooking.event.Show;
import com.ticketbooking.event.ShowRepository;
import com.ticketbooking.seat.SeatService;
import com.ticketbooking.seat.SeatStatus;
import com.ticketbooking.seat.ShowSeat;
import com.ticketbooking.seat.ShowSeatRepository;
import com.ticketbooking.seat.dto.HoldSeatsRequest;
import com.ticketbooking.user.Role;
import com.ticketbooking.user.User;
import com.ticketbooking.user.UserRepository;
import com.ticketbooking.venue.Seat;
import com.ticketbooking.venue.SeatCategory;
import com.ticketbooking.venue.SeatCategoryRepository;
import com.ticketbooking.venue.SeatRepository;
import com.ticketbooking.venue.Venue;
import com.ticketbooking.venue.VenueRepository;
import com.ticketbooking.waitlist.WaitlistEntry;
import com.ticketbooking.waitlist.WaitlistEntryRepository;
import com.ticketbooking.waitlist.WaitlistOffer;
import com.ticketbooking.waitlist.WaitlistOfferRepository;
import com.ticketbooking.waitlist.WaitlistOfferStatus;
import com.ticketbooking.waitlist.WaitlistService;
import com.ticketbooking.waitlist.WaitlistStatus;
import com.ticketbooking.waitlist.dto.JoinWaitlistRequest;
import com.ticketbooking.waitlist.dto.WaitlistResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class WaitlistLifecycleTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private SeatService seatService;

    @Autowired
    private WaitlistService waitlistService;

    @Autowired
    private ShowSeatRepository showSeatRepository;

    @Autowired
    private ShowRepository showRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private VenueRepository venueRepository;

    @Autowired
    private SeatCategoryRepository seatCategoryRepository;

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private com.ticketbooking.booking.BookingRepository bookingRepository;

    @Autowired
    private com.ticketbooking.booking.BookingSeatRepository bookingSeatRepository;

    @Autowired
    private WaitlistEntryRepository waitlistEntryRepository;

    @Autowired
    private WaitlistOfferRepository waitlistOfferRepository;

    private User userA;
    private User userB;
    private Show show;
    private ShowSeat showSeat;
    private SeatCategory category;

    @BeforeEach
    void setUp() {
        waitlistOfferRepository.deleteAll();
        waitlistEntryRepository.deleteAll();
        bookingSeatRepository.deleteAll();
        bookingRepository.deleteAll();
        showSeatRepository.deleteAll();
        showRepository.deleteAll();
        eventRepository.deleteAll();
        seatRepository.deleteAll();
        venueRepository.deleteAll();
        userRepository.deleteAll();

        User organiser = userRepository.save(User.builder()
                .name("Organiser")
                .email("organiser@test.com")
                .password("pwd")
                .role(Role.ORGANISER)
                .build());

        userA = userRepository.save(User.builder()
                .name("User A")
                .email("usera@test.com")
                .password("pwd")
                .role(Role.CUSTOMER)
                .build());

        userB = userRepository.save(User.builder()
                .name("User B")
                .email("userb@test.com")
                .password("pwd")
                .role(Role.CUSTOMER)
                .build());

        Venue venue = venueRepository.save(Venue.builder().name("Cinema Hall").location("Downtown").build());
        category = seatCategoryRepository.findByNameIgnoreCase("VIP")
                .orElseGet(() -> seatCategoryRepository.save(SeatCategory.builder().name("VIP").build()));

        Seat seat = seatRepository.save(Seat.builder()
                .venue(venue)
                .rowNumber("A")
                .seatNumber(1)
                .category(category)
                .build());

        Event event = eventRepository.save(Event.builder()
                .title("Waitlist Movie Test")
                .eventType(EventType.MOVIE)
                .organiser(organiser)
                .venue(venue)
                .build());

        show = showRepository.save(Show.builder()
                .event(event)
                .venue(venue)
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(1).plusHours(2))
                .build());

        showSeat = showSeatRepository.save(ShowSeat.builder()
                .show(show)
                .seat(seat)
                .status(SeatStatus.AVAILABLE)
                .price(BigDecimal.valueOf(150.00))
                .build());
    }

    private void authenticateAs(User user) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities())
        );
    }

    @Test
    @DisplayName("Complete Waitlist Lifecycle: Booking by User A -> Waitlist Join by User B -> Cancellation by User A -> Auto-offer to User B -> Booking by User B")
    void testWaitlistLifecycle() {
        // Step 1: User A holds and books the seat
        authenticateAs(userA);
        seatService.holdSeats(show.getId(), HoldSeatsRequest.builder().showSeatIds(List.of(showSeat.getId())).build());

        BookingResponse bookingA = bookingService.createBooking(CreateBookingRequest.builder()
                .showId(show.getId())
                .showSeatIds(List.of(showSeat.getId()))
                .build());

        assertNotNull(bookingA);
        assertEquals(BookingStatus.CONFIRMED, bookingA.getStatus());
        assertNotNull(bookingA.getQrCodeBase64(), "QR code must be generated with booking");

        ShowSeat bookedSeat = showSeatRepository.findById(showSeat.getId()).orElseThrow();
        assertEquals(SeatStatus.BOOKED, bookedSeat.getStatus());

        // Step 2: User B attempts to join waitlist for this sold-out category
        authenticateAs(userB);
        WaitlistResponse waitlistB = waitlistService.joinWaitlist(show.getId(),
                JoinWaitlistRequest.builder().seatCategoryId(category.getId()).build());

        assertNotNull(waitlistB);
        assertEquals(1, waitlistB.getPosition());
        assertEquals(WaitlistStatus.WAITING, waitlistB.getStatus());

        // Step 3: User A cancels booking -> triggers auto-reallocation to User B
        authenticateAs(userA);
        BookingResponse cancelledBooking = bookingService.cancelBooking(bookingA.getId());
        assertEquals(BookingStatus.CANCELLED, cancelledBooking.getStatus());

        // Step 4: Verify User B automatically received a time-limited offer!
        WaitlistEntry updatedEntryB = waitlistEntryRepository.findById(waitlistB.getId()).orElseThrow();
        assertEquals(WaitlistStatus.OFFERED, updatedEntryB.getStatus(), "Waitlist entry must transition to OFFERED");

        List<WaitlistOffer> offers = waitlistOfferRepository.findByWaitlistEntryUserIdAndStatus(userB.getId(), WaitlistOfferStatus.PENDING);
        assertEquals(1, offers.size());
        WaitlistOffer offerForB = offers.get(0);
        assertEquals(showSeat.getId(), offerForB.getShowSeat().getId());
        assertEquals(WaitlistOfferStatus.PENDING, offerForB.getStatus());

        // The seat in DB should now be HELD for User B
        ShowSeat heldForB = showSeatRepository.findById(showSeat.getId()).orElseThrow();
        assertEquals(SeatStatus.HELD, heldForB.getStatus());
        assertEquals(userB.getId(), heldForB.getHeldBy().getId());

        // Step 5: User B completes checkout using the waitlist offer
        authenticateAs(userB);
        BookingResponse bookingB = bookingService.createBooking(CreateBookingRequest.builder()
                .showId(show.getId())
                .showSeatIds(List.of(showSeat.getId()))
                .waitlistOfferId(offerForB.getId())
                .build());

        assertNotNull(bookingB);
        assertEquals(BookingStatus.CONFIRMED, bookingB.getStatus());
        assertEquals(userB.getId(), bookingB.getUserId());

        // Verify offer marked ACCEPTED and waitlist marked COMPLETED
        WaitlistOffer acceptedOffer = waitlistOfferRepository.findById(offerForB.getId()).orElseThrow();
        assertEquals(WaitlistOfferStatus.ACCEPTED, acceptedOffer.getStatus());

        WaitlistEntry completedEntry = waitlistEntryRepository.findById(waitlistB.getId()).orElseThrow();
        assertEquals(WaitlistStatus.COMPLETED, completedEntry.getStatus());
    }
}
