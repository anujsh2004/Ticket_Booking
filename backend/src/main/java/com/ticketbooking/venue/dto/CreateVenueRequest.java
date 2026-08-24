package com.ticketbooking.venue.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateVenueRequest {

    @NotBlank(message = "Venue name is required")
    private String name;

    @NotBlank(message = "Venue location is required")
    private String location;
}
