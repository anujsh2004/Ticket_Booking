package com.ticketbooking.booking;

import com.ticketbooking.booking.dto.BookingResponse;
import com.ticketbooking.booking.dto.CreateBookingRequest;
import com.ticketbooking.booking.dto.OrganiserDashboardStatsResponse;
import com.ticketbooking.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping("/bookings")
    public ResponseEntity<ApiResponse<BookingResponse>> createBooking(@Valid @RequestBody CreateBookingRequest request) {
        BookingResponse response = bookingService.createBooking(request);
        return ResponseEntity.ok(ApiResponse.success("Booking confirmed successfully", response));
    }

    @PostMapping("/bookings/{id}/cancel")
    public ResponseEntity<ApiResponse<BookingResponse>> cancelBooking(@PathVariable Long id) {
        BookingResponse response = bookingService.cancelBooking(id);
        return ResponseEntity.ok(ApiResponse.success("Booking cancelled successfully", response));
    }

    @GetMapping("/bookings/my")
    public ResponseEntity<ApiResponse<List<BookingResponse>>> getMyBookings() {
        List<BookingResponse> response = bookingService.getMyBookings();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/bookings/{id}")
    public ResponseEntity<ApiResponse<BookingResponse>> getBookingById(@PathVariable Long id) {
        BookingResponse response = bookingService.getBookingById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/organiser/dashboard")
    @PreAuthorize("hasAnyRole('ORGANISER', 'ADMIN')")
    public ResponseEntity<ApiResponse<OrganiserDashboardStatsResponse>> getOrganiserDashboard() {
        OrganiserDashboardStatsResponse response = bookingService.getOrganiserStats();
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
