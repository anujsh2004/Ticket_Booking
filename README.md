# 🎟️ Ticket Booking System

[![Live Demo](https://img.shields.io/badge/Live_Demo-slowmotickets.vercel.app-000000?style=for-the-badge&logo=vercel&logoColor=white)](https://slowmotickets.vercel.app/)
[![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.4.3-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-19-61DAFB?style=for-the-badge&logo=react&logoColor=black)](https://react.dev/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-4169E1?style=for-the-badge&logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Redis](https://img.shields.io/badge/Redis-7-DC382D?style=for-the-badge&logo=redis&logoColor=white)](https://redis.io/)

A production-grade, concurrency-safe ticket booking platform for movies and concerts built with **Java Spring Boot 3**, **PostgreSQL**, **Redis**, **WebSocket/STOMP**, **ZXing**, and **React**.

> 🌐 **Live Demo:** [https://slowmotickets.vercel.app/](https://slowmotickets.vercel.app/)

---

## 🌟 Key Features

- 🛡️ **Concurrency Protection**: Strict database row-level locking (`SELECT ... FOR UPDATE`) prevents double booking under heavy concurrent traffic (tested with 100 simultaneous requests).
- ⏱️ **Seat Hold & TTL Auto-Release**: Temporary seat reservation (10-min TTL) backed by Redis + PostgreSQL with automatic abandonment release.
- 📋 **Intelligent FIFO Waitlist**: Category-based waitlist queue with automated seat reassignment and time-limited booking offers (15-min TTL) upon cancellations.
- ⚡ **Real-Time Visual Seat Map**: Instant color-coded seat updates (`AVAILABLE` 🟢 $\rightarrow$ `HELD` 🟡 $\rightarrow$ `BOOKED` 🔴) via WebSocket / STOMP without page reload.
- 🔲 **ZXing QR Code & Email Tickets**: Base64/PNG QR codes encoding booking references generated on confirmation and dispatched via transactional emails.
- 🔐 **Role-Based Access Control (RBAC)**: JWT authentication for `CUSTOMER`, `ORGANISER`, and `ADMIN`.

---

## 🏗️ System Architecture

```
                                  +-------------------+
                                  |   React + Vite    |
                                  |   (Tailwind CSS)  |
                                  +---------+---------+
                                            |
                                  REST APIs | WebSockets (STOMP)
                                            v
+-----------------------------------------------------------------------------------+
|                           SPRING BOOT (Modular Monolith)                          |
|                                                                                   |
|  +--------------+  +--------------+  +---------------+  +----------------------+  |
|  | Auth Module  |  | Venue Module |  | Event Module  |  | Seat & Hold Module   |  |
|  +--------------+  +--------------+  +---------------+  +----------------------+  |
|                                                                                   |
|  +--------------+  +--------------+  +---------------+  +----------------------+  |
|  | Booking Mod  |  | Waitlist Mod |  | WebSocket Svc |  | QR & Email Service   |  |
|  +--------------+  +--------------+  +---------------+  +----------------------+  |
+---------------------------+-----------------------------------+-------------------+
                            |                                   |
                            v                                   v
                 +--------------------+              +--------------------+
                 |     PostgreSQL     |              |       Redis        |
                 | (Source of Truth)  |              |  (Holds & TTLs)    |
                 +--------------------+              +--------------------+
```

---

## 🚀 Quick Start Guide

### Prerequisites
- Java 21 or Java 25
- Docker & Docker Compose
- Node.js 18+ (for Frontend)

### 1. Clone the Repository
```bash
git clone https://github.com/anujsh2004/Ticket_Booking.git
cd Ticket_Booking
```

### 2. Start PostgreSQL and Redis via Docker
```bash
docker compose up -d
```

### 3. Run the Backend
```bash
cd backend
./mvnw spring-boot:run
```
*(On Windows: `.\mvnw.cmd spring-boot:run`)*

The backend will automatically start on `http://localhost:8080` and seed initial demo data:
- **Admin**: `admin@tickets.com` / `admin123`
- **Organiser**: `organiser@tickets.com` / `organiser123`
- **Customer**: `customer@tickets.com` / `customer123`

---

## 🧪 Running Automated Tests

Run the complete test suite including concurrency and waitlist lifecycle tests:
```bash
cd backend
./mvnw test
```

### Verified Test Highlights:
- `SeatConcurrencyTest`: Simulates 100 simultaneous concurrent threads competing for the same seat $\rightarrow$ **1 Success, 99 Failures**.
- `WaitlistLifecycleTest`: Full end-to-end flow:
  1. Customer A books seat $\rightarrow$ Sold out.
  2. Customer B joins waitlist (Position #1).
  3. Customer A cancels $\rightarrow$ System automatically locks seat for Customer B and creates a 15-min offer.
  4. Customer B completes checkout $\rightarrow$ Confirmed booking with QR code.

---

## 📡 REST API Reference

### Authentication
| Method | Endpoint | Access | Description |
|---|---|---|---|
| `POST` | `/api/auth/register` | Public | Register new user (`CUSTOMER`, `ORGANISER`, `ADMIN`) |
| `POST` | `/api/auth/login` | Public | Authenticate user and receive JWT token |
| `GET` | `/api/auth/me` | Authenticated | Get current user profile |

### Venues & Seats (Admin)
| Method | Endpoint | Access | Description |
|---|---|---|---|
| `POST` | `/api/venues` | Admin | Create a new venue |
| `GET` | `/api/venues` | Public | List all venues |
| `GET` | `/api/venues/{id}` | Public | Get venue details and seating layout |
| `POST` | `/api/venues/{id}/seats` | Admin | Batch configure seat rows and categories |

### Events & Shows (Organiser)
| Method | Endpoint | Access | Description |
|---|---|---|---|
| `POST` | `/api/events` | Organiser | Create a new event (`MOVIE` or `CONCERT`) |
| `GET` | `/api/events` | Public | Browse and search events |
| `GET` | `/api/events/{id}` | Public | Get event details with shows |
| `POST` | `/api/events/{id}/shows` | Organiser | Schedule a show with category pricing |
| `GET` | `/api/shows/{id}` | Public | Get show details and availability |

### Seat Map & Holds (Customer)
| Method | Endpoint | Access | Description |
|---|---|---|---|
| `GET` | `/api/shows/{showId}/seats` | Public | Get real-time seat map grid |
| `POST` | `/api/shows/{showId}/holds` | Customer | Hold selected seats (10-min TTL) |
| `DELETE` | `/api/holds/{showSeatId}` | Customer | Manually release seat hold |

### Bookings & Checkout (Customer & Organiser)
| Method | Endpoint | Access | Description |
|---|---|---|---|
| `POST` | `/api/bookings` | Customer | Complete checkout on held seats (Generates QR & Email) |
| `GET` | `/api/bookings/my` | Customer | View customer booking history |
| `GET` | `/api/bookings/{id}` | Customer | View booking details and ticket |
| `POST` | `/api/bookings/{id}/cancel`| Customer | Cancel booking (Triggers waitlist reallocation) |
| `GET` | `/api/organiser/dashboard` | Organiser | View ticket sales and revenue summary |

### Waitlists (Customer)
| Method | Endpoint | Access | Description |
|---|---|---|---|
| `POST` | `/api/shows/{showId}/waitlist` | Customer | Join waitlist for a specific category |
| `GET` | `/api/waitlist/my` | Customer | View active waitlist positions |
| `GET` | `/api/waitlist/offers/{offerId}` | Customer | View time-limited seat offer details |

---

## 🗄️ Database Schema

```
Users (id, name, email, password_hash, role, created_at, updated_at)
Venues (id, name, location, created_at)
SeatCategories (id, name: VIP | PREMIUM | STANDARD)
Seats (id, venue_id, row_number, seat_number, category_id)
Events (id, organiser_id, venue_id, title, description, image_url, event_type, created_at)
Shows (id, event_id, venue_id, start_time, end_time, created_at)
ShowSeats (id, show_id, seat_id, status: AVAILABLE | HELD | BOOKED, price, held_by, hold_expires_at, version)
Bookings (id, booking_reference, user_id, show_id, total_amount, status: CONFIRMED | CANCELLED, qr_code_base64, created_at, cancelled_at)
BookingSeats (id, booking_id, show_seat_id, price)
WaitlistEntries (id, show_id, user_id, seat_category_id, position, status: WAITING | OFFERED | COMPLETED | EXPIRED | CANCELLED, created_at)
WaitlistOffers (id, waitlist_entry_id, show_seat_id, expires_at, status: PENDING | ACCEPTED | EXPIRED, created_at)
```
