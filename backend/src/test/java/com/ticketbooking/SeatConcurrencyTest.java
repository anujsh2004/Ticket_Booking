package com.ticketbooking;

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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
class SeatConcurrencyTest {

    @Autowired
    private SeatService seatService;

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
    private com.ticketbooking.waitlist.WaitlistEntryRepository waitlistEntryRepository;

    @Autowired
    private com.ticketbooking.waitlist.WaitlistOfferRepository waitlistOfferRepository;

    private Show testShow;
    private ShowSeat targetSeat;
    private List<User> testUsers;

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

        // 1. Create Organiser & 100 test Customers
        User organiser = userRepository.save(User.builder()
                .name("Organiser")
                .email("org@test.com")
                .password("pwd")
                .role(Role.ORGANISER)
                .build());

        testUsers = new ArrayList<>();
        for (int i = 1; i <= 100; i++) {
            testUsers.add(userRepository.save(User.builder()
                    .name("User " + i)
                    .email("user" + i + "@test.com")
                    .password("pwd")
                    .role(Role.CUSTOMER)
                    .build()));
        }

        // 2. Create Venue, Seat Category, Base Seat
        Venue venue = venueRepository.save(Venue.builder().name("Test Arena").location("Location").build());
        SeatCategory category = seatCategoryRepository.findByNameIgnoreCase("PREMIUM")
                .orElseGet(() -> seatCategoryRepository.save(SeatCategory.builder().name("PREMIUM").build()));

        Seat seat = seatRepository.save(Seat.builder()
                .venue(venue)
                .rowNumber("A")
                .seatNumber(1)
                .category(category)
                .build());

        // 3. Create Event & Show
        Event event = eventRepository.save(Event.builder()
                .title("Concurrency Test Event")
                .eventType(EventType.CONCERT)
                .organiser(organiser)
                .venue(venue)
                .build());

        testShow = showRepository.save(Show.builder()
                .event(event)
                .venue(venue)
                .startTime(LocalDateTime.now().plusDays(2))
                .endTime(LocalDateTime.now().plusDays(2).plusHours(3))
                .build());

        targetSeat = showSeatRepository.save(ShowSeat.builder()
                .show(testShow)
                .seat(seat)
                .status(SeatStatus.AVAILABLE)
                .price(BigDecimal.valueOf(100.00))
                .build());
    }

    @Test
    @DisplayName("Simultaneous 100 threads attempting to hold the same seat must produce exactly 1 success and 99 failures")
    void testSimultaneous100SeatHoldRequests() throws InterruptedException {
        int threadCount = 100;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            final User user = testUsers.get(i);
            executor.submit(() -> {
                try {
                    // Set security context for thread
                    SecurityContextHolder.getContext().setAuthentication(
                            new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities())
                    );
                    startLatch.await(); // Wait for all threads to be ready

                    HoldSeatsRequest req = HoldSeatsRequest.builder()
                            .showSeatIds(List.of(targetSeat.getId()))
                            .build();

                    seatService.holdSeats(testShow.getId(), req);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                } finally {
                    SecurityContextHolder.clearContext();
                    endLatch.countDown();
                }
            });
        }

        startLatch.countDown(); // Fire all 100 threads simultaneously!
        endLatch.await();
        executor.shutdown();

        System.out.println("Concurrency Test Result -> Successes: " + successCount.get() + ", Failures: " + failCount.get());

        assertEquals(1, successCount.get(), "Exactly one user should successfully hold the seat");
        assertEquals(99, failCount.get(), "All other 99 concurrent requests must fail");

        ShowSeat finalSeatState = showSeatRepository.findById(targetSeat.getId()).orElseThrow();
        assertEquals(SeatStatus.HELD, finalSeatState.getStatus());
    }
}
