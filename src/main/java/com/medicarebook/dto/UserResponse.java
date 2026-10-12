package com.medicarebook.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.medicarebook.entity.User;

import java.time.LocalDateTime;

public class UserResponse {

    private Long id;

    @JsonProperty("_id")
    private String mongoId;

    private String name;
    private String email;
    private String role;
    private LocalDateTime createdAt;

    public UserResponse() {
    }

    public UserResponse(User user) {
        if (user != null) {
            this.id = user.getId();
            this.mongoId = user.getId() != null ? user.getId().toString() : null;
            this.name = user.getName();
            this.email = user.getEmail();
            this.role = user.getRole() != null ? user.getRole().name().toLowerCase() : "user";
            this.createdAt = user.getCreatedAt();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
        this.mongoId = id != null ? id.toString() : null;
    }

    public String get_id() {
        return mongoId;
    }

    public void set_id(String mongoId) {
        this.mongoId = mongoId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
