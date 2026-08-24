package com.ticketbooking.event.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShowResponse {
    private Long id;
    private Long eventId;
    private String eventTitle;
    private Long venueId;
    private String venueName;
    private String venueLocation;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private int totalSeats;
    private int availableSeats;
    private int heldSeats;
    private int bookedSeats;
}
