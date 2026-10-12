package com.medicarebook.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AuthResponse {

    private String token;
    private String type = "Bearer";
    private Long id;

    @JsonProperty("_id")
    private String mongoId;

    private String name;
    private String email;
    private String role; // "user", "doctor", "admin"

    public AuthResponse() {
    }

    public AuthResponse(String token, Long id, String name, String email, String role) {
        this.token = token;
        this.id = id;
        this.mongoId = id != null ? id.toString() : null;
        this.name = name;
        this.email = email;
        this.role = role != null ? role.toLowerCase() : "user";
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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
}
