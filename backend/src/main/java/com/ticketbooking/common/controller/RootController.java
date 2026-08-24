package com.ticketbooking.common.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class RootController {

    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> rootHealthCheck() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "application", "SlowMO Concurrency-Protected Ticket Booking Engine",
                "version", "1.0.0",
                "eventsEndpoint", "/api/events",
                "showsEndpoint", "/api/shows",
                "authEndpoint", "/api/auth/login"
        ));
    }
}
