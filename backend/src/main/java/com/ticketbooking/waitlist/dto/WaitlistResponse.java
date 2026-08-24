package com.ticketbooking.waitlist.dto;

import com.ticketbooking.waitlist.WaitlistStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WaitlistResponse {
    private Long id;
    private Long showId;
    private String eventTitle;
    private String venueName;
    private LocalDateTime showStartTime;
    private Long seatCategoryId;
    private String seatCategoryName;
    private Integer position;
    private WaitlistStatus status;
    private LocalDateTime createdAt;
    private WaitlistOfferResponse activeOffer;
}
