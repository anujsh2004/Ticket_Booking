package com.ticketbooking.seat;

import com.ticketbooking.common.dto.ApiResponse;
import com.ticketbooking.seat.dto.HoldSeatsRequest;
import com.ticketbooking.seat.dto.HoldSeatsResponse;
import com.ticketbooking.seat.dto.ShowSeatResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SeatController {

    private final SeatService seatService;

    @GetMapping("/shows/{showId}/seats")
    public ResponseEntity<ApiResponse<List<ShowSeatResponse>>> getSeatMap(@PathVariable Long showId) {
        List<ShowSeatResponse> response = seatService.getSeatMap(showId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/shows/{showId}/holds")
    public ResponseEntity<ApiResponse<HoldSeatsResponse>> holdSeats(
            @PathVariable Long showId,
            @Valid @RequestBody HoldSeatsRequest request
    ) {
        HoldSeatsResponse response = seatService.holdSeats(showId, request);
        return ResponseEntity.ok(ApiResponse.success("Seats successfully held", response));
    }

    @DeleteMapping("/holds/{showSeatId}")
    public ResponseEntity<ApiResponse<Void>> releaseHold(@PathVariable Long showSeatId) {
        seatService.releaseHold(showSeatId);
        return ResponseEntity.ok(ApiResponse.success("Seat hold released successfully", null));
    }
}
