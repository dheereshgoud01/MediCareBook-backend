package com.medicarebook.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.medicarebook.entity.DoctorProfile;

import java.time.LocalDateTime;

public class DoctorProfileResponse {

    private Long id;

    @JsonProperty("_id")
    private String mongoId;

    private Long userId;
    private String name;
    private String specialization;
    private String availability;
    private Double fees;
    private String status;
    private LocalDateTime createdAt;

    public DoctorProfileResponse() {
    }

    public DoctorProfileResponse(DoctorProfile doctor) {
        if (doctor != null) {
            this.id = doctor.getId();
            this.mongoId = doctor.getId() != null ? doctor.getId().toString() : null;
            this.userId = doctor.getUser() != null ? doctor.getUser().getId() : null;
            this.name = doctor.getName();
            this.specialization = doctor.getSpecialization();
            this.availability = doctor.getAvailability();
            this.fees = doctor.getFees();
            this.status = doctor.getStatus() != null ? doctor.getStatus().name().toLowerCase() : "pending";
            this.createdAt = doctor.getCreatedAt();
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

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public String getAvailability() {
        return availability;
    }

    public void setAvailability(String availability) {
        this.availability = availability;
    }

    public Double getFees() {
        return fees;
    }

    public void setFees(Double fees) {
        this.fees = fees;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
