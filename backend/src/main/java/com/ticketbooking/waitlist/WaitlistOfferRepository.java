package com.ticketbooking.waitlist;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface WaitlistOfferRepository extends JpaRepository<WaitlistOffer, Long> {

    List<WaitlistOffer> findByStatusAndExpiresAtBefore(WaitlistOfferStatus status, LocalDateTime dateTime);

    Optional<WaitlistOffer> findByWaitlistEntryIdAndStatus(Long waitlistEntryId, WaitlistOfferStatus status);

    List<WaitlistOffer> findByWaitlistEntryUserIdAndStatus(Long userId, WaitlistOfferStatus status);
}
