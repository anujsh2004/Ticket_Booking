package com.ticketbooking.venue;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
    name = "seats",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"venue_id", "row_number", "seat_number"})
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venue_id", nullable = false)
    @JsonIgnoreProperties({"seats", "hibernateLazyInitializer"})
    private Venue venue;

    @Column(name = "row_number", nullable = false, length = 10)
    private String rowNumber; // e.g., "A", "B", "C"

    @Column(name = "seat_number", nullable = false)
    private Integer seatNumber; // e.g., 1, 2, 3...

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id", nullable = false)
    private SeatCategory category;
}
