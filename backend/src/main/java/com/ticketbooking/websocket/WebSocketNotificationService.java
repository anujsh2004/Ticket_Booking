package com.ticketbooking.websocket;

import com.ticketbooking.seat.SeatStatus;
import com.ticketbooking.seat.ShowSeat;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebSocketNotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    public void broadcastSeatUpdates(Long showId, List<ShowSeat> updatedSeats) {
        try {
            List<SeatStatusUpdateMessage.SeatUpdateDetail> details = updatedSeats.stream()
                    .map(seat -> SeatStatusUpdateMessage.SeatUpdateDetail.builder()
                            .showSeatId(seat.getId())
                            .rowNumber(seat.getSeat().getRowNumber())
                            .seatNumber(seat.getSeat().getSeatNumber())
                            .status(seat.getStatus())
                            .heldByUserId(seat.getHeldBy() != null ? seat.getHeldBy().getId() : null)
                            .build())
                    .collect(Collectors.toList());

            SeatStatusUpdateMessage message = SeatStatusUpdateMessage.builder()
                    .showId(showId)
                    .updatedSeats(details)
                    .timestamp(LocalDateTime.now())
                    .build();

            String destination = "/topic/shows/" + showId + "/seats";
            messagingTemplate.convertAndSend(destination, message);
            log.info("Broadcasted {} seat updates to destination {}", details.size(), destination);
        } catch (Exception e) {
            log.error("Failed to broadcast WebSocket seat updates for showId: {}", showId, e);
        }
    }

    public void broadcastSingleSeatUpdate(Long showId, ShowSeat seat) {
        broadcastSeatUpdates(showId, List.of(seat));
    }
}
