package com.ticketbooking.booking.dto;

import com.ticketbooking.booking.BookingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingResponse {
    private Long id;
    private String bookingReference;
    private Long userId;
    private String userName;
    private String userEmail;
    private Long showId;
    private String eventTitle;
    private String venueName;
    private String venueLocation;
    private LocalDateTime showStartTime;
    private BigDecimal totalAmount;
    private BookingStatus status;
    private String qrCodeBase64;
    private LocalDateTime createdAt;
    private LocalDateTime cancelledAt;
    private List<BookingSeatResponse> seats;
}
