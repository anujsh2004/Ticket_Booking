package com.ticketbooking.seat.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HoldSeatsRequest {

    @NotEmpty(message = "At least one show seat ID is required")
    private List<Long> showSeatIds;
}
