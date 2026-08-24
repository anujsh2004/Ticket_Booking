package com.ticketbooking.websocket;

import com.ticketbooking.seat.SeatStatus;
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
public class SeatStatusUpdateMessage {
    private Long showId;
    private List<SeatUpdateDetail> updatedSeats;
    private LocalDateTime timestamp;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SeatUpdateDetail {
        private Long showSeatId;
        private String rowNumber;
        private Integer seatNumber;
        private SeatStatus status;
        private Long heldByUserId;
    }
}
