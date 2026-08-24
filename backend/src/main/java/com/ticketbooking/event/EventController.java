package com.ticketbooking.event;

import com.ticketbooking.common.dto.ApiResponse;
import com.ticketbooking.event.dto.CreateEventRequest;
import com.ticketbooking.event.dto.CreateShowRequest;
import com.ticketbooking.event.dto.EventResponse;
import com.ticketbooking.event.dto.ShowResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ORGANISER', 'ADMIN')")
    public ResponseEntity<ApiResponse<EventResponse>> createEvent(@Valid @RequestBody CreateEventRequest request) {
        EventResponse response = eventService.createEvent(request);
        return ResponseEntity.ok(ApiResponse.success("Event created successfully", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<EventResponse>>> getAllEvents(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) EventType type
    ) {
        List<EventResponse> response = eventService.getAllEvents(search, type);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EventResponse>> getEventById(@PathVariable Long id) {
        EventResponse response = eventService.getEventById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/{id}/shows")
    @PreAuthorize("hasAnyRole('ORGANISER', 'ADMIN')")
    public ResponseEntity<ApiResponse<ShowResponse>> createShow(
            @PathVariable Long id,
            @Valid @RequestBody CreateShowRequest request
    ) {
        ShowResponse response = eventService.createShow(id, request);
        return ResponseEntity.ok(ApiResponse.success("Show created and seats initialized successfully", response));
    }

    @GetMapping("/{id}/shows")
    public ResponseEntity<ApiResponse<List<ShowResponse>>> getShowsByEvent(@PathVariable Long id) {
        List<ShowResponse> response = eventService.getShowsByEvent(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
