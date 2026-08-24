package com.ticketbooking.venue;

import com.ticketbooking.common.dto.ApiResponse;
import com.ticketbooking.venue.dto.CreateSeatsBatchRequest;
import com.ticketbooking.venue.dto.CreateVenueRequest;
import com.ticketbooking.venue.dto.SeatResponse;
import com.ticketbooking.venue.dto.VenueResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/venues")
@RequiredArgsConstructor
public class VenueController {

    private final VenueService venueService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<VenueResponse>> createVenue(@Valid @RequestBody CreateVenueRequest request) {
        VenueResponse response = venueService.createVenue(request);
        return ResponseEntity.ok(ApiResponse.success("Venue created successfully", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<VenueResponse>>> getAllVenues() {
        List<VenueResponse> response = venueService.getAllVenues();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<VenueResponse>> getVenueById(@PathVariable Long id) {
        VenueResponse response = venueService.getVenueById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/{id}/seats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<SeatResponse>>> configureSeats(
            @PathVariable Long id,
            @Valid @RequestBody CreateSeatsBatchRequest request) {
        List<SeatResponse> response = venueService.configureSeatsBatch(id, request);
        return ResponseEntity.ok(ApiResponse.success("Seats configured successfully", response));
    }

    @GetMapping("/{id}/seats")
    public ResponseEntity<ApiResponse<List<SeatResponse>>> getSeatsByVenue(@PathVariable Long id) {
        List<SeatResponse> response = venueService.getSeatsByVenue(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
