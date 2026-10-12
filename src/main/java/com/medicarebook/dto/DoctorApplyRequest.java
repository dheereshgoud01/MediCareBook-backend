package com.medicarebook.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class DoctorApplyRequest {

    private Long userId;

    @NotBlank(message = "Doctor name is required")
    private String name;

    @NotBlank(message = "Specialization is required")
    private String specialization;

    @NotBlank(message = "Availability schedule is required")
    private String availability;

    @NotNull(message = "Consultation fees are required")
    @Positive(message = "Consultation fees must be greater than zero")
    private Double fees;

    public DoctorApplyRequest() {
    }

    public DoctorApplyRequest(Long userId, String name, String specialization, String availability, Double fees) {
        this.userId = userId;
        this.name = name;
        this.specialization = specialization;
        this.availability = availability;
        this.fees = fees;
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
}
