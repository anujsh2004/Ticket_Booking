package com.ticketbooking.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingSeatResponse {
    private Long id;
    private Long showSeatId;
    private String rowNumber;
    private Integer seatNumber;
    private String categoryName;
    private BigDecimal price;
}
