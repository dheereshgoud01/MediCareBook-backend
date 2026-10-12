package com.medicarebook.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.medicarebook.entity.Appointment;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class AppointmentResponse {

    private Long id;

    @JsonProperty("_id")
    private String mongoId;

    private Long userId;
    private Long doctorId;
    private String userName;
    private String doctorName;
    private LocalDate date;
    private String time;
    private String status;
    private String notes;
    private LocalDateTime createdAt;

    public AppointmentResponse() {
    }

    public AppointmentResponse(Appointment appointment) {
        if (appointment != null) {
            this.id = appointment.getId();
            this.mongoId = appointment.getId() != null ? appointment.getId().toString() : null;
            this.userId = appointment.getUser() != null ? appointment.getUser().getId() : null;
            this.doctorId = appointment.getDoctor() != null ? appointment.getDoctor().getId() : null;
            this.userName = appointment.getUserName();
            this.doctorName = appointment.getDoctorName();
            this.date = appointment.getAppointmentDate();
            this.time = appointment.getAppointmentTime();
            this.status = appointment.getStatus() != null ? appointment.getStatus().name().toLowerCase() : "pending";
            this.notes = appointment.getNotes();
            this.createdAt = appointment.getCreatedAt();
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

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
