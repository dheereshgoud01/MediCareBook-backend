package com.medicarebook.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class DoctorProfileUpdateRequest {

    @NotBlank(message = "Availability schedule is required")
    private String availability;

    @NotNull(message = "Consultation fees are required")
    @Positive(message = "Consultation fees must be greater than zero")
    private Double fees;

    private String specialization;

    public DoctorProfileUpdateRequest() {
    }

    public DoctorProfileUpdateRequest(String availability, Double fees) {
        this.availability = availability;
        this.fees = fees;
    }

    public DoctorProfileUpdateRequest(String availability, Double fees, String specialization) {
        this.availability = availability;
        this.fees = fees;
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

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }
}
