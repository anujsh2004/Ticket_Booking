package com.ticketbooking.event;

import com.ticketbooking.common.exception.BadRequestException;
import com.ticketbooking.common.exception.ResourceNotFoundException;
import com.ticketbooking.event.dto.*;
import com.ticketbooking.seat.SeatStatus;
import com.ticketbooking.seat.ShowSeat;
import com.ticketbooking.seat.ShowSeatRepository;
import com.ticketbooking.user.User;
import com.ticketbooking.user.UserService;
import com.ticketbooking.venue.Seat;
import com.ticketbooking.venue.SeatRepository;
import com.ticketbooking.venue.Venue;
import com.ticketbooking.venue.VenueService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final ShowRepository showRepository;
    private final ShowSeatRepository showSeatRepository;
    private final SeatRepository seatRepository;
    private final VenueService venueService;
    private final UserService userService;

    @Transactional
    public EventResponse createEvent(CreateEventRequest request) {
        User organiser = userService.getCurrentUser();
        Venue venue = venueService.getVenueEntity(request.getVenueId());

        Event event = Event.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .imageUrl(request.getImageUrl())
                .eventType(request.getEventType())
                .organiser(organiser)
                .venue(venue)
                .build();

        event = eventRepository.save(event);
        return mapToEventResponse(event, true);
    }

    public List<EventResponse> getAllEvents(String query, EventType eventType) {
        List<Event> events;
        if (query != null && !query.isBlank()) {
            events = eventRepository.findByTitleContainingIgnoreCase(query.trim());
        } else if (eventType != null) {
            events = eventRepository.findByEventType(eventType);
        } else {
            events = eventRepository.findAll();
        }

        return events.stream()
                .map(event -> mapToEventResponse(event, true))
                .collect(Collectors.toList());
    }

    public EventResponse getEventById(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + id));
        return mapToEventResponse(event, true);
    }

    public Event getEventEntity(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + id));
    }

    @Transactional
    public ShowResponse createShow(Long eventId, CreateShowRequest request) {
        Event event = getEventEntity(eventId);
        User currentUser = userService.getCurrentUser();

        // Security check: Only the organiser who created the event or an ADMIN can add shows
        if (!event.getOrganiser().getId().equals(currentUser.getId()) && !currentUser.getRole().name().equals("ADMIN")) {
            throw new BadRequestException("You can only add shows to your own events");
        }

        if (request.getEndTime().isBefore(request.getStartTime())) {
            throw new BadRequestException("End time must be after start time");
        }

        Venue venue = (request.getVenueId() != null)
                ? venueService.getVenueEntity(request.getVenueId())
                : event.getVenue();

        Show show = Show.builder()
                .event(event)
                .venue(venue)
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .build();

        show = showRepository.save(show);

        // Fetch venue seats
        List<Seat> seats = seatRepository.findByVenueIdOrderByRowNumberAscSeatNumberAsc(venue.getId());
        if (seats.isEmpty()) {
            throw new BadRequestException("Cannot create show: Venue has no configured seats. Please configure seats first.");
        }

        // Build price lookup map from request
        Map<String, BigDecimal> categoryPricing = new HashMap<>();
        for (CategoryPricingDto pricing : request.getPricing()) {
            categoryPricing.put(pricing.getCategoryName().toUpperCase().trim(), pricing.getPrice());
        }

        // Generate ShowSeat entities
        List<ShowSeat> showSeats = new ArrayList<>();
        for (Seat seat : seats) {
            String categoryName = seat.getCategory().getName().toUpperCase().trim();
            BigDecimal price = categoryPricing.getOrDefault(categoryName, BigDecimal.valueOf(100.00));

            ShowSeat showSeat = ShowSeat.builder()
                    .show(show)
                    .seat(seat)
                    .status(SeatStatus.AVAILABLE)
                    .price(price)
                    .build();
            showSeats.add(showSeat);
        }

        showSeatRepository.saveAll(showSeats);
        return mapToShowResponse(show);
    }

    public List<ShowResponse> getShowsByEvent(Long eventId) {
        getEventEntity(eventId); // check exists
        return showRepository.findByEventIdOrderByStartTimeAsc(eventId).stream()
                .map(this::mapToShowResponse)
                .collect(Collectors.toList());
    }

    public ShowResponse getShowById(Long showId) {
        Show show = showRepository.findById(showId)
                .orElseThrow(() -> new ResourceNotFoundException("Show not found with id: " + showId));
        return mapToShowResponse(show);
    }

    public Show getShowEntity(Long showId) {
        return showRepository.findById(showId)
                .orElseThrow(() -> new ResourceNotFoundException("Show not found with id: " + showId));
    }

    public EventResponse mapToEventResponse(Event event, boolean includeShows) {
        List<ShowResponse> shows = includeShows
                ? showRepository.findByEventIdOrderByStartTimeAsc(event.getId()).stream()
                    .map(this::mapToShowResponse)
                    .collect(Collectors.toList())
                : null;

        return EventResponse.builder()
                .id(event.getId())
                .title(event.getTitle())
                .description(event.getDescription())
                .imageUrl(event.getImageUrl())
                .eventType(event.getEventType())
                .organiserId(event.getOrganiser() != null ? event.getOrganiser().getId() : null)
                .organiserName(event.getOrganiser() != null ? event.getOrganiser().getName() : null)
                .venueId(event.getVenue() != null ? event.getVenue().getId() : null)
                .venueName(event.getVenue() != null ? event.getVenue().getName() : null)
                .venueLocation(event.getVenue() != null ? event.getVenue().getLocation() : null)
                .createdAt(event.getCreatedAt())
                .shows(shows)
                .build();
    }

    public ShowResponse mapToShowResponse(Show show) {
        long available = showSeatRepository.countByShowIdAndStatus(show.getId(), SeatStatus.AVAILABLE);
        long held = showSeatRepository.countByShowIdAndStatus(show.getId(), SeatStatus.HELD);
        long booked = showSeatRepository.countByShowIdAndStatus(show.getId(), SeatStatus.BOOKED);
        int total = (int) (available + held + booked);

        return ShowResponse.builder()
                .id(show.getId())
                .eventId(show.getEvent().getId())
                .eventTitle(show.getEvent().getTitle())
                .venueId(show.getVenue().getId())
                .venueName(show.getVenue().getName())
                .venueLocation(show.getVenue().getLocation())
                .startTime(show.getStartTime())
                .endTime(show.getEndTime())
                .totalSeats(total)
                .availableSeats((int) available)
                .heldSeats((int) held)
                .bookedSeats((int) booked)
                .build();
    }
}
