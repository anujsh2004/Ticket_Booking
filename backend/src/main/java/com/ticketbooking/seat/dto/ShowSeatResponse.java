package com.ticketbooking.seat.dto;

import com.ticketbooking.seat.SeatStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShowSeatResponse {
    private Long id; // ShowSeat id
    private Long seatId; // Base Seat id
    private String rowNumber;
    private Integer seatNumber;
    private Long categoryId;
    private String categoryName;
    private BigDecimal price;
    private SeatStatus status;
    private Long heldByUserId;
    private LocalDateTime holdExpiresAt;
    private boolean isHeldByMe;
}
