package com.medicarebook.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public class AiChatRequest {

    @NotBlank(message = "Prompt cannot be empty")
    private String message;

    private Long userId;

    public AiChatRequest() {
    }

    public AiChatRequest(String message, Long userId) {
        this.message = message;
        this.userId = userId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
