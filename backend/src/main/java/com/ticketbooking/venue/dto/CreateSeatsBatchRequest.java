package com.ticketbooking.venue.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateSeatsBatchRequest {

    @NotNull(message = "Seat rows configuration is required")
    private List<RowConfig> rows;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RowConfig {
        @NotBlank(message = "Row letter/number is required")
        private String rowNumber; // e.g. "A"

        @Min(value = 1, message = "Start seat number must be at least 1")
        private int startSeatNumber; // e.g. 1

        @Min(value = 1, message = "End seat number must be at least 1")
        private int endSeatNumber; // e.g. 10

        @NotBlank(message = "Category name is required (VIP, PREMIUM, STANDARD)")
        private String categoryName;
    }
}
