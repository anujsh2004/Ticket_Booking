package com.ticketbooking.venue;

import com.ticketbooking.common.exception.BadRequestException;
import com.ticketbooking.common.exception.ResourceNotFoundException;
import com.ticketbooking.venue.dto.CreateSeatsBatchRequest;
import com.ticketbooking.venue.dto.CreateVenueRequest;
import com.ticketbooking.venue.dto.SeatResponse;
import com.ticketbooking.venue.dto.VenueResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VenueService {

    private final VenueRepository venueRepository;
    private final SeatRepository seatRepository;
    private final SeatCategoryRepository seatCategoryRepository;

    @Transactional
    public VenueResponse createVenue(CreateVenueRequest request) {
        Venue venue = Venue.builder()
                .name(request.getName())
                .location(request.getLocation())
                .build();
        venue = venueRepository.save(venue);
        return mapToVenueResponse(venue, true);
    }

    public List<VenueResponse> getAllVenues() {
        return venueRepository.findAll().stream()
                .map(v -> mapToVenueResponse(v, false))
                .collect(Collectors.toList());
    }

    public VenueResponse getVenueById(Long id) {
        Venue venue = venueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Venue not found with id: " + id));
        return mapToVenueResponse(venue, true);
    }

    public Venue getVenueEntity(Long id) {
        return venueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Venue not found with id: " + id));
    }

    @Transactional
    public List<SeatResponse> configureSeatsBatch(Long venueId, CreateSeatsBatchRequest request) {
        Venue venue = getVenueEntity(venueId);
        List<Seat> newSeats = new ArrayList<>();

        for (CreateSeatsBatchRequest.RowConfig row : request.getRows()) {
            SeatCategory category = seatCategoryRepository.findByNameIgnoreCase(row.getCategoryName())
                    .orElseGet(() -> seatCategoryRepository.save(
                            SeatCategory.builder().name(row.getCategoryName().toUpperCase().trim()).build()
                    ));

            for (int seatNum = row.getStartSeatNumber(); seatNum <= row.getEndSeatNumber(); seatNum++) {
                if (seatRepository.existsByVenueIdAndRowNumberAndSeatNumber(venueId, row.getRowNumber(), seatNum)) {
                    continue; // Skip existing seat
                }
                Seat seat = Seat.builder()
                        .venue(venue)
                        .rowNumber(row.getRowNumber().toUpperCase().trim())
                        .seatNumber(seatNum)
                        .category(category)
                        .build();
                newSeats.add(seat);
            }
        }

        if (newSeats.isEmpty()) {
            throw new BadRequestException("No new seats created (they may already exist)");
        }

        List<Seat> savedSeats = seatRepository.saveAll(newSeats);
        return savedSeats.stream().map(this::mapToSeatResponse).collect(Collectors.toList());
    }

    public List<SeatResponse> getSeatsByVenue(Long venueId) {
        getVenueEntity(venueId); // check exists
        return seatRepository.findByVenueIdOrderByRowNumberAscSeatNumberAsc(venueId).stream()
                .map(this::mapToSeatResponse)
                .collect(Collectors.toList());
    }

    public SeatCategory getOrCreateCategory(String name) {
        return seatCategoryRepository.findByNameIgnoreCase(name)
                .orElseGet(() -> seatCategoryRepository.save(SeatCategory.builder().name(name.toUpperCase().trim()).build()));
    }

    public VenueResponse mapToVenueResponse(Venue venue, boolean includeSeats) {
        List<SeatResponse> seats = includeSeats
                ? seatRepository.findByVenueIdOrderByRowNumberAscSeatNumberAsc(venue.getId()).stream()
                    .map(this::mapToSeatResponse)
                    .collect(Collectors.toList())
                : null;

        int totalSeats = (seats != null) ? seats.size() : (venue.getSeats() != null ? venue.getSeats().size() : 0);

        return VenueResponse.builder()
                .id(venue.getId())
                .name(venue.getName())
                .location(venue.getLocation())
                .totalSeats(totalSeats)
                .createdAt(venue.getCreatedAt())
                .seats(seats)
                .build();
    }

    public SeatResponse mapToSeatResponse(Seat seat) {
        return SeatResponse.builder()
                .id(seat.getId())
                .rowNumber(seat.getRowNumber())
                .seatNumber(seat.getSeatNumber())
                .categoryId(seat.getCategory() != null ? seat.getCategory().getId() : null)
                .categoryName(seat.getCategory() != null ? seat.getCategory().getName() : null)
                .build();
    }
}
