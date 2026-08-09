package com.clearledger.user_service.controller;

import com.clearledger.user_service.dto.AuthResponse;
import com.clearledger.user_service.dto.LoginRequest;
import com.clearledger.user_service.dto.RegisterRequest;
import com.clearledger.user_service.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@RequestBody Map<String, String> body) {
        String refreshToken = body.get("refreshToken");
        return ResponseEntity.ok(authService.refresh(refreshToken));
    }

    // Simple endpoint to verify the service is up
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("user-service is running");
    }
}

/*
@Valid on the request body triggers all the validation annotations from your DTOs automatically.
If email is blank or password too short, Spring returns a 400 with the error message before your code even runs.
*/