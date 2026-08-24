package com.ticketbooking;

import com.ticketbooking.auth.AuthService;
import com.ticketbooking.auth.dto.AuthResponse;
import com.ticketbooking.auth.dto.LoginRequest;
import com.ticketbooking.auth.dto.RegisterRequest;
import com.ticketbooking.common.exception.BadRequestException;
import com.ticketbooking.user.Role;
import com.ticketbooking.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class AuthServiceTest {

    @Autowired
    private AuthService authService;

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

    @Autowired
    private com.ticketbooking.seat.ShowSeatRepository showSeatRepository;

    @Autowired
    private com.ticketbooking.event.ShowRepository showRepository;

    @Autowired
    private com.ticketbooking.event.EventRepository eventRepository;

    @Autowired
    private com.ticketbooking.venue.SeatRepository seatRepository;

    @Autowired
    private com.ticketbooking.venue.VenueRepository venueRepository;

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
    }

    @Test
    @DisplayName("Should successfully register customer and return valid JWT")
    void testRegisterCustomer() {
        RegisterRequest request = RegisterRequest.builder()
                .name("Alice Smith")
                .email("alice@example.com")
                .password("password123")
                .role(Role.CUSTOMER)
                .build();

        AuthResponse response = authService.register(request);

        assertNotNull(response);
        assertNotNull(response.getToken());
        assertEquals("alice@example.com", response.getEmail());
        assertEquals(Role.CUSTOMER, response.getRole());
    }

    @Test
    @DisplayName("Should reject duplicate email registration")
    void testDuplicateEmailRegistration() {
        RegisterRequest request = RegisterRequest.builder()
                .name("Bob")
                .email("bob@example.com")
                .password("password123")
                .role(Role.CUSTOMER)
                .build();

        authService.register(request);

        assertThrows(BadRequestException.class, () -> authService.register(request));
    }

    @Test
    @DisplayName("Should successfully login registered user")
    void testLoginSuccess() {
        RegisterRequest registerReq = RegisterRequest.builder()
                .name("Charlie")
                .email("charlie@example.com")
                .password("secret123")
                .role(Role.ORGANISER)
                .build();

        authService.register(registerReq);

        LoginRequest loginReq = LoginRequest.builder()
                .email("charlie@example.com")
                .password("secret123")
                .build();

        AuthResponse response = authService.login(loginReq);
        assertNotNull(response);
        assertNotNull(response.getToken());
        assertEquals(Role.ORGANISER, response.getRole());
    }
}
