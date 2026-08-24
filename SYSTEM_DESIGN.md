# Ticket Booking System — System Design & Architecture Document

## 1. System Overview & Problem Statement
High-demand event ticketing platforms face severe concurrency and resource-allocation challenges:
- **Simultaneous Race Conditions**: Hundreds or thousands of users attempting to hold or purchase the exact same seat within milliseconds.
- **Abandoned Checkout Wastage**: Customers abandoning shopping carts, resulting in orphaned seat holds unless strictly released by an automated TTL (Time-To-Live) mechanism.
- **Sold-Out Allocation Inefficiency**: Last-minute booking cancellations going unutilized when an automated, instant queue-reallocation system is absent.

To solve these challenges, this system implements a high-performance **Modular Monolith** in **Java Spring Boot**, **PostgreSQL** as the relational source of truth, **Redis** for distributed caching and hold tracking, **WebSockets (STOMP)** for real-time client state synchronization, and **ZXing** for booking QR verification.

---

## 2. Concurrency Protection & Transaction Isolation

```
User A ──┐
         ├─► [POST /api/shows/{id}/holds] ──► [SELECT ... FOR UPDATE (Pessimistic Lock)] ──► HELD (Success)
User B ──┘                                                                              └──► CONFLICT 409 (Rejected)
```

### Mechanism:
1. **Pessimistic Row-Level Locking**:
   When a user requests a hold on one or more seat IDs, the transaction queries the database utilizing `@Lock(LockModeType.PESSIMISTIC_WRITE)`:
   ```sql
   SELECT * FROM show_seats WHERE id IN (:seatIds) FOR UPDATE;
   ```
2. **Atomic Verification**:
   Within the exclusive lock boundary, the backend verifies that every requested seat has status `AVAILABLE`. If any seat is `HELD` (by another active user) or `BOOKED`, the transaction immediately rolls back and returns HTTP `409 CONFLICT` (`SeatUnavailableException`).
3. **Optimistic Versioning Fallback**:
   Each `ShowSeat` entity is augmented with a JPA `@Version` column (`version BIGINT`), providing a two-tier safety net against split-brain scenarios or stale reads.

---

## 3. Seat Hold & Distributed TTL Mechanism

```
[Hold Request] ──► DB: Status = HELD, expires_at = NOW + 10m
               ──► Redis: SET hold:show:{id}:seat:{id} TTL 600s
               ──► WebSocket: Broadcast HELD state to /topic/shows/{id}/seats
```

1. **Configurable TTL**: When a hold succeeds, `hold_expires_at` is set to `LocalDateTime.now().plusMinutes(10)` and stored persistently in PostgreSQL.
2. **Fast In-Memory State**: Redis key `hold:show:{showId}:seat:{seatId}` is created with an exact 600-second TTL.
3. **Automated Recovery Scheduler**:
   A scheduled background engine (`SeatHoldScheduler` running `@Scheduled(fixedRate = 5000)`) scans for holds where `hold_expires_at < NOW()`.
4. **Instant State Broadcast**:
   Whenever a seat is held or released, `WebSocketNotificationService` transmits the updated seat object to all active viewers subscribed to `/topic/shows/{showId}/seats`.

---

## 4. Intelligent Waitlist & Time-Limited Offer Auto-Assignment

```
[Booking Cancelled] ──► Find Earliest WAITING in category (FIFO)
                     ├──► [Found] ──► Create WaitlistOffer (TTL 15m)
                     │             ──► DB: Seat HELD for Waitlist User
                     │             ──► Email Notification with One-Click Checkout URL
                     └──► [None]  ──► Revert Seat to AVAILABLE & Broadcast WebSocket
```

1. **Category-Based FIFO Queue**:
   When an event or seat tier (e.g. VIP, PREMIUM) sells out, customers join a waitlist per show and seat category. Queue position is strictly assigned sequentially (`position = MAX(position) + 1`).
2. **Event-Driven Cancellation Reallocation**:
   When a confirmed booking is cancelled:
   - The system retrieves the first `WAITING` candidate for that show and category.
   - If a waiting customer is found, a `WaitlistOffer` is generated with a strict 15-minute expiration timestamp (`expires_at`).
   - The seat is locked as `HELD` specifically for that customer, and an email notification with a direct checkout link is dispatched asynchronously.
   - The candidate's waitlist status advances to `OFFERED`.
3. **Cascading Expiration**:
   If the offer expires without completion, `SeatHoldScheduler` marks the offer and entry as `EXPIRED`, and instantly cascades the seat to the next waitlisted customer in line.

---

## 5. Security & Role-Based Access Control (RBAC)
- **Stateless JWT**: Standard Bearer token authentication signed via HMAC-SHA256 (`jjwt 0.12.6`).
- **Granular Roles**:
  - `CUSTOMER`: Browse events, select visual seats, hold seats, complete checkout, manage booking cancellations, and join/accept waitlists.
  - `ORGANISER`: Create events, schedule shows, set tier pricing, inspect live sales and revenue statistics.
  - `ADMIN`: Global venue management, custom seat row/grid layout design, and user oversight.

---

## 6. Real-Time WebSocket & Push Architecture
- **STOMP Protocol over SockJS**: Frontend clients establish real-time duplex channels (`/ws`).
- **Topic Channel**: `/topic/shows/{showId}/seats`
- **Zero-Latency UX**: Any hold, release, booking, or cancellation instantly repaints color indicators on all connected screens without requiring browser reloads.
