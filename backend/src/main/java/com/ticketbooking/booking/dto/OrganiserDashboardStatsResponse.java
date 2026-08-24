package com.ticketbooking.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrganiserDashboardStatsResponse {
    private BigDecimal totalRevenue;
    private long totalTicketsSold;
    private long totalEvents;
    private long activeShows;
    private List<BookingResponse> recentBookings;
}
