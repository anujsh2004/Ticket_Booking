package com.ticketbooking.event.dto;

import com.ticketbooking.event.EventType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateEventRequest {

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    private String imageUrl;

    @NotNull(message = "Event type is required (MOVIE, CONCERT)")
    private EventType eventType;

    @NotNull(message = "Venue ID is required")
    private Long venueId;
}
