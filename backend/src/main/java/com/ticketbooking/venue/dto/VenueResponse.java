package com.ticketbooking.venue.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VenueResponse {
    private Long id;
    private String name;
    private String location;
    private int totalSeats;
    private LocalDateTime createdAt;
    private List<SeatResponse> seats;
}
