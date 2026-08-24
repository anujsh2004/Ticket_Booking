package com.ticketbooking.waitlist;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.ticketbooking.event.Show;
import com.ticketbooking.user.User;
import com.ticketbooking.venue.SeatCategory;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "waitlist_entries",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"show_id", "user_id", "seat_category_id"})
    },
    indexes = {
        @Index(name = "idx_waitlist_queue", columnList = "show_id, seat_category_id, position, status")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WaitlistEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "show_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer"})
    private Show show;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties({"password", "authorities", "hibernateLazyInitializer"})
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "seat_category_id", nullable = false)
    private SeatCategory seatCategory;

    @Column(nullable = false)
    private Integer position;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private WaitlistStatus status = WaitlistStatus.WAITING;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
