package com.medicarebook.controller;

import com.medicarebook.dto.*;
import com.medicarebook.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication & User Management", description = "Endpoints for user registration, login, and admin account management")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new Patient or Doctor account", description = "Registers user and returns initial JWT token. Prevents unauthorized admin escalation.")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    @Operation(summary = "User login with email and password", description = "Authenticates credentials and returns JWT bearer token along with user profile.")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List all registered accounts (Admin only)", description = "Returns all users excluding sensitive credentials.")
    public ResponseEntity<List<UserResponse>> listUsers() {
        return ResponseEntity.ok(authService.listUsers());
    }

    @PutMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update user details or role (Admin only)")
    public ResponseEntity<UserResponse> updateUser(@PathVariable Long userId, @RequestBody UserUpdateRequest request) {
        return ResponseEntity.ok(authService.updateUser(userId, request));
    }

    @DeleteMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete user account with cascading cleanup (Admin only)")
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable Long userId) {
        authService.deleteUser(userId);
        return ResponseEntity.ok(Map.of("message", "User and associated profiles/appointments deleted successfully."));
    }
}
