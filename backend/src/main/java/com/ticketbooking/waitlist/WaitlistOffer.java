package com.ticketbooking.waitlist;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.ticketbooking.seat.ShowSeat;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "waitlist_offers",
    indexes = {
        @Index(name = "idx_offer_expires", columnList = "expires_at, status")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WaitlistOffer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "waitlist_entry_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer"})
    private WaitlistEntry waitlistEntry;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "show_seat_id", nullable = false)
    private ShowSeat showSeat;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private WaitlistOfferStatus status = WaitlistOfferStatus.PENDING;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
