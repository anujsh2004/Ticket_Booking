package com.ticketbooking.waitlist.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JoinWaitlistRequest {

    @NotNull(message = "Seat category ID is required")
    private Long seatCategoryId;
}
