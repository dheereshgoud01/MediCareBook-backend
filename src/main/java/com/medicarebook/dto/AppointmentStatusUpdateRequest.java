package com.medicarebook.dto;

import jakarta.validation.constraints.NotBlank;

public class AppointmentStatusUpdateRequest {

    @NotBlank(message = "Status is required")
    private String status;

    public AppointmentStatusUpdateRequest() {
    }

    public AppointmentStatusUpdateRequest(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
