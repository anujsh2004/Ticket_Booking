package com.ticketbooking.event.dto;

import com.ticketbooking.event.EventType;
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
public class EventResponse {
    private Long id;
    private String title;
    private String description;
    private String imageUrl;
    private EventType eventType;
    private Long organiserId;
    private String organiserName;
    private Long venueId;
    private String venueName;
    private String venueLocation;
    private LocalDateTime createdAt;
    private List<ShowResponse> shows;
}
