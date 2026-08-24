package com.ticketbooking.waitlist.dto;

import com.ticketbooking.waitlist.WaitlistOfferStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WaitlistOfferResponse {
    private Long id;
    private Long waitlistEntryId;
    private Long showSeatId;
    private String seatRow;
    private Integer seatNumber;
    private String categoryName;
    private BigDecimal price;
    private LocalDateTime expiresAt;
    private long remainingSeconds;
    private WaitlistOfferStatus status;
}
