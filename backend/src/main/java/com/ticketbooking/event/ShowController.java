package com.ticketbooking.event;

import com.ticketbooking.common.dto.ApiResponse;
import com.ticketbooking.event.dto.ShowResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/shows")
@RequiredArgsConstructor
public class ShowController {

    private final EventService eventService;

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ShowResponse>> getShowById(@PathVariable Long id) {
        ShowResponse response = eventService.getShowById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
