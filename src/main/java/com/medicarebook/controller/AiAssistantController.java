package com.medicarebook.controller;

import com.medicarebook.dto.AiChatRequest;
import com.medicarebook.dto.AiChatResponse;
import com.medicarebook.service.AiAssistantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@Tag(name = "AI Healthcare Assistant", description = "Endpoints for RAG platform navigation, specialty recommendations, and query assistance")
public class AiAssistantController {

    private final AiAssistantService aiAssistantService;

    public AiAssistantController(AiAssistantService aiAssistantService) {
        this.aiAssistantService = aiAssistantService;
    }

    @PostMapping("/chat")
    @Operation(summary = "Query the AI Healthcare Assistant", description = "Provides symptom-to-specialty guidance, available doctor suggestions, and platform knowledge.")
    public ResponseEntity<AiChatResponse> chat(@Valid @RequestBody AiChatRequest request) {
        AiChatResponse response = aiAssistantService.processMessage(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    @Operation(summary = "Check AI service health status")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "engine", "MediCareBook-RAG-Assistant-1.0",
                "activeKnowledgeBase", "Clinical Specialties & Platform Guidelines"
        ));
    }
}
