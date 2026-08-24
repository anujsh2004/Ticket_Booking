package com.ticketbooking.common.config;

import com.ticketbooking.event.Event;
import com.ticketbooking.event.EventRepository;
import com.ticketbooking.event.EventType;
import com.ticketbooking.event.Show;
import com.ticketbooking.event.ShowRepository;
import com.ticketbooking.seat.SeatStatus;
import com.ticketbooking.seat.ShowSeat;
import com.ticketbooking.seat.ShowSeatRepository;
import com.ticketbooking.user.Role;
import com.ticketbooking.user.User;
import com.ticketbooking.user.UserRepository;
import com.ticketbooking.venue.Seat;
import com.ticketbooking.venue.SeatCategory;
import com.ticketbooking.venue.SeatCategoryRepository;
import com.ticketbooking.venue.SeatRepository;
import com.ticketbooking.venue.Venue;
import com.ticketbooking.venue.VenueRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@org.springframework.context.annotation.Profile("!test")
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final VenueRepository venueRepository;
    private final SeatCategoryRepository seatCategoryRepository;
    private final SeatRepository seatRepository;
    private final EventRepository eventRepository;
    private final ShowRepository showRepository;
    private final ShowSeatRepository showSeatRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("Database already seeded. Skipping initial data loading.");
            return;
        }

        log.info("Seeding initial demo data...");

        // 1. Seed Users
        User admin = User.builder()
                .name("System Admin")
                .email("admin@tickets.com")
                .password(passwordEncoder.encode("admin123"))
                .role(Role.ADMIN)
                .build();

        User organiser = User.builder()
                .name("Live Nation Entertainment")
                .email("organiser@tickets.com")
                .password(passwordEncoder.encode("organiser123"))
                .role(Role.ORGANISER)
                .build();

        User customer = User.builder()
                .name("John Doe")
                .email("customer@tickets.com")
                .password(passwordEncoder.encode("customer123"))
                .role(Role.CUSTOMER)
                .build();

        userRepository.saveAll(List.of(admin, organiser, customer));

        // 2. Seed Seat Categories
        SeatCategory vip = seatCategoryRepository.save(SeatCategory.builder().name("VIP").build());
        SeatCategory premium = seatCategoryRepository.save(SeatCategory.builder().name("PREMIUM").build());
        SeatCategory standard = seatCategoryRepository.save(SeatCategory.builder().name("STANDARD").build());

        // 3. Seed Venues & Seat Layouts
        Venue venue1 = venueRepository.save(Venue.builder()
                .name("PVR IMAX Arena")
                .location("Lower Parel, Mumbai")
                .build());

        Venue venue2 = venueRepository.save(Venue.builder()
                .name("DY Patil Stadium")
                .location("Navi Mumbai")
                .build());

        List<Seat> seatsVenue1 = new ArrayList<>();
        // Row A: VIP (1 to 10)
        for (int i = 1; i <= 10; i++) {
            seatsVenue1.add(Seat.builder().venue(venue1).rowNumber("A").seatNumber(i).category(vip).build());
        }
        // Row B: PREMIUM (1 to 10)
        for (int i = 1; i <= 10; i++) {
            seatsVenue1.add(Seat.builder().venue(venue1).rowNumber("B").seatNumber(i).category(premium).build());
        }
        // Row C: STANDARD (1 to 10)
        for (int i = 1; i <= 10; i++) {
            seatsVenue1.add(Seat.builder().venue(venue1).rowNumber("C").seatNumber(i).category(standard).build());
        }
        seatRepository.saveAll(seatsVenue1);

        List<Seat> seatsVenue2 = new ArrayList<>();
        for (int i = 1; i <= 8; i++) {
            seatsVenue2.add(Seat.builder().venue(venue2).rowNumber("A").seatNumber(i).category(vip).build());
            seatsVenue2.add(Seat.builder().venue(venue2).rowNumber("B").seatNumber(i).category(premium).build());
            seatsVenue2.add(Seat.builder().venue(venue2).rowNumber("C").seatNumber(i).category(standard).build());
        }
        seatRepository.saveAll(seatsVenue2);

        // 4. Seed Events
        Event movieEvent = eventRepository.save(Event.builder()
                .title("Avengers: Secret Wars")
                .description("Earth's mightiest heroes clash across the multiverse in the ultimate cinematic event.")
                .imageUrl("https://images.unsplash.com/photo-1536440136628-849c177e76a1?auto=format&fit=crop&w=800&q=80")
                .eventType(EventType.MOVIE)
                .organiser(organiser)
                .venue(venue1)
                .build());

        Event concertEvent = eventRepository.save(Event.builder()
                .title("Coldplay: Music of the Spheres World Tour")
                .description("Experience an unforgettable night of cosmic lights, fireworks, and legendary music.")
                .imageUrl("https://images.unsplash.com/photo-1470225620780-dba8ba36b745?auto=format&fit=crop&w=800&q=80")
                .eventType(EventType.CONCERT)
                .organiser(organiser)
                .venue(venue2)
                .build());

        // 5. Seed Shows & ShowSeats
        LocalDateTime tomorrowEvening = LocalDateTime.now().plusDays(1).withHour(19).withMinute(0).withSecond(0).withNano(0);
        Show movieShow = showRepository.save(Show.builder()
                .event(movieEvent)
                .venue(venue1)
                .startTime(tomorrowEvening)
                .endTime(tomorrowEvening.plusHours(3))
                .build());

        List<ShowSeat> showSeats1 = new ArrayList<>();
        for (Seat s : seatsVenue1) {
            BigDecimal price = switch (s.getCategory().getName()) {
                case "VIP" -> BigDecimal.valueOf(180.00);
                case "PREMIUM" -> BigDecimal.valueOf(95.00);
                default -> BigDecimal.valueOf(45.00);
            };
            showSeats1.add(ShowSeat.builder()
                    .show(movieShow)
                    .seat(s)
                    .status(SeatStatus.AVAILABLE)
                    .price(price)
                    .build());
        }
        showSeatRepository.saveAll(showSeats1);

        LocalDateTime concertTime = LocalDateTime.now().plusDays(3).withHour(20).withMinute(0).withSecond(0).withNano(0);
        Show concertShow = showRepository.save(Show.builder()
                .event(concertEvent)
                .venue(venue2)
                .startTime(concertTime)
                .endTime(concertTime.plusHours(4))
                .build());

        List<ShowSeat> showSeats2 = new ArrayList<>();
        for (Seat s : seatsVenue2) {
            BigDecimal price = switch (s.getCategory().getName()) {
                case "VIP" -> BigDecimal.valueOf(350.00);
                case "PREMIUM" -> BigDecimal.valueOf(180.00);
                default -> BigDecimal.valueOf(75.00);
            };
            showSeats2.add(ShowSeat.builder()
                    .show(concertShow)
                    .seat(s)
                    .status(SeatStatus.AVAILABLE)
                    .price(price)
                    .build());
        }
        showSeatRepository.saveAll(showSeats2);

        log.info("Demo data successfully seeded! Admin: admin@tickets.com, Organiser: organiser@tickets.com, Customer: customer@tickets.com (password: admin123 / organiser123 / customer123)");
    }
}
