package com.ticketbooking.auth;

import com.ticketbooking.auth.dto.AuthResponse;
import com.ticketbooking.auth.dto.LoginRequest;
import com.ticketbooking.auth.dto.RegisterRequest;
import com.ticketbooking.common.dto.ApiResponse;
import com.ticketbooking.user.User;
import com.ticketbooking.user.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.ok(ApiResponse.success("User registered successfully", response));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<User>> getCurrentUser() {
        User user = userService.getCurrentUser();
        return ResponseEntity.ok(ApiResponse.success(user));
    }
}
