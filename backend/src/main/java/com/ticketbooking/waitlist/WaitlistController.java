package com.ticketbooking.waitlist;

import com.ticketbooking.common.dto.ApiResponse;
import com.ticketbooking.waitlist.dto.JoinWaitlistRequest;
import com.ticketbooking.waitlist.dto.WaitlistOfferResponse;
import com.ticketbooking.waitlist.dto.WaitlistResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class WaitlistController {

    private final WaitlistService waitlistService;

    @PostMapping("/shows/{showId}/waitlist")
    public ResponseEntity<ApiResponse<WaitlistResponse>> joinWaitlist(
            @PathVariable Long showId,
            @Valid @RequestBody JoinWaitlistRequest request
    ) {
        WaitlistResponse response = waitlistService.joinWaitlist(showId, request);
        return ResponseEntity.ok(ApiResponse.success("Successfully joined waitlist", response));
    }

    @GetMapping("/waitlist/my")
    public ResponseEntity<ApiResponse<List<WaitlistResponse>>> getMyWaitlists() {
        List<WaitlistResponse> response = waitlistService.getMyWaitlistEntries();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/waitlist/offers/{offerId}")
    public ResponseEntity<ApiResponse<WaitlistOfferResponse>> getOfferDetails(@PathVariable Long offerId) {
        WaitlistOffer offer = waitlistService.getOfferById(offerId);
        return ResponseEntity.ok(ApiResponse.success(waitlistService.mapToOfferResponse(offer)));
    }
}
