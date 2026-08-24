package com.ticketbooking.booking.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateBookingRequest {

    @NotNull(message = "Show ID is required")
    private Long showId;

    @NotEmpty(message = "At least one show seat ID is required")
    private List<Long> showSeatIds;

    private Long waitlistOfferId; // Optional: If booking was triggered from a waitlist offer
}
