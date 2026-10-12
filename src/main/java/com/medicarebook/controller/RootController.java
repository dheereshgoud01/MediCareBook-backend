package com.medicarebook.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class RootController {

    @GetMapping("/")
    public ResponseEntity<String> root() {
        return ResponseEntity.ok("MediCareBook AI Spring Boot API is running successfully.");
    }

    @GetMapping("/api/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "medicarebook-backend",
                "framework", "Spring Boot 3.3.4",
                "database", "MySQL 8.0"
        ));
    }
}
