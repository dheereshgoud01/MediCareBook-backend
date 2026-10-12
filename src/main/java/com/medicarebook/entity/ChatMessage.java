package com.medicarebook.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "chat_messages")
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "user_email")
    private String userEmail;

    @Column(name = "user_prompt", nullable = false, length = 2000)
    private String userPrompt;

    @Column(name = "assistant_response", nullable = false, length = 4000)
    private String assistantResponse;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public ChatMessage() {
    }

    public ChatMessage(Long userId, String userEmail, String userPrompt, String assistantResponse) {
        this.userId = userId;
        this.userEmail = userEmail;
        this.userPrompt = userPrompt;
        this.assistantResponse = assistantResponse;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUserPrompt() {
        return userPrompt;
    }

    public void setUserPrompt(String userPrompt) {
        this.userPrompt = userPrompt;
    }

    public String getAssistantResponse() {
        return assistantResponse;
    }

    public void setAssistantResponse(String assistantResponse) {
        this.assistantResponse = assistantResponse;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
