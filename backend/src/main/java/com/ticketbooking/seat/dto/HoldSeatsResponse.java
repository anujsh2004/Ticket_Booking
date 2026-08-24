package com.ticketbooking.seat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HoldSeatsResponse {
    private Long showId;
    private List<ShowSeatResponse> heldSeats;
    private BigDecimal totalAmount;
    private LocalDateTime expiresAt;
    private long remainingSeconds;
}
