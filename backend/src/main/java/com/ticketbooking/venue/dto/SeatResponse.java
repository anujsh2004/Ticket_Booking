package com.ticketbooking.venue.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeatResponse {
    private Long id;
    private String rowNumber;
    private Integer seatNumber;
    private Long categoryId;
    private String categoryName;
}
