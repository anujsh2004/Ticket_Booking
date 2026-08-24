package com.ticketbooking.seat;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.ticketbooking.event.Show;
import com.ticketbooking.user.User;
import com.ticketbooking.venue.Seat;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "show_seats",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"show_id", "seat_id"})
    },
    indexes = {
        @Index(name = "idx_show_seat_status", columnList = "show_id, status"),
        @Index(name = "idx_show_seat_expires", columnList = "hold_expires_at")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShowSeat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "show_id", nullable = false)
    @JsonIgnoreProperties({"showSeats", "hibernateLazyInitializer"})
    private Show show;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "seat_id", nullable = false)
    private Seat seat;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private SeatStatus status = SeatStatus.AVAILABLE;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "held_by")
    @JsonIgnoreProperties({"password", "authorities", "hibernateLazyInitializer"})
    private User heldBy;

    @Column(name = "hold_expires_at")
    private LocalDateTime holdExpiresAt;

    @Version
    private Long version;
}
