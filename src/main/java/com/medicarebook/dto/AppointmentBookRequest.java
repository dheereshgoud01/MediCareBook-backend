package com.medicarebook.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class AppointmentBookRequest {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Doctor ID is required")
    private Long doctorId;

    private String userName;
    private String doctorName;

    @NotBlank(message = "Appointment date is required")
    private String date;

    @NotBlank(message = "Appointment time is required")
    private String time;

    private String notes;

    public AppointmentBookRequest() {
    }

    public AppointmentBookRequest(Long userId, Long doctorId, String userName, String doctorName, String date, String time) {
        this.userId = userId;
        this.doctorId = doctorId;
        this.userName = userName;
        this.doctorName = doctorName;
        this.date = date;
        this.time = time;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(Long doctorId) {
        this.doctorId = doctorId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
