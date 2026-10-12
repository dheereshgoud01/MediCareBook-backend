package com.medicarebook.controller;

import com.medicarebook.dto.*;
import com.medicarebook.service.AppointmentService;
import com.medicarebook.service.DoctorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/doctors")
@Tag(name = "Doctor & Appointment Operations", description = "Endpoints for doctor directories, profiles, appointment booking, and lifecycle status management")
public class DoctorController {

    private final DoctorService doctorService;
    private final AppointmentService appointmentService;

    public DoctorController(DoctorService doctorService, AppointmentService appointmentService) {
        this.doctorService = doctorService;
        this.appointmentService = appointmentService;
    }

    @PostMapping("/apply")
    @Operation(summary = "Submit or update doctor application profile")
    public ResponseEntity<DoctorProfileResponse> applyDoctor(@Valid @RequestBody DoctorApplyRequest request) {
        DoctorProfileResponse response = doctorService.applyDoctor(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/list")
    @Operation(summary = "List all doctors (Public directory, cached)")
    public ResponseEntity<List<DoctorProfileResponse>> listDoctors() {
        return ResponseEntity.ok(doctorService.listDoctors());
    }

    @GetMapping("/profile/user/{userId}")
    @Operation(summary = "Get doctor profile linked to user account ID")
    public ResponseEntity<DoctorProfileResponse> getDoctorProfileByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(doctorService.getDoctorProfileByUserId(userId));
    }

    @PutMapping("/profile/{doctorId}")
    @Operation(summary = "Update doctor availability and consultation fee")
    public ResponseEntity<DoctorProfileResponse> updateDoctorProfile(
            @PathVariable Long doctorId,
            @Valid @RequestBody DoctorProfileUpdateRequest request) {
        return ResponseEntity.ok(doctorService.updateDoctorProfile(doctorId, request));
    }

    @PostMapping("/book")
    @Operation(summary = "Book doctor appointment slot with double-booking prevention")
    public ResponseEntity<AppointmentResponse> bookAppointment(@Valid @RequestBody AppointmentBookRequest request) {
        AppointmentResponse response = appointmentService.bookAppointment(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/history/user/{userId}")
    @Operation(summary = "Get appointment history for a patient")
    public ResponseEntity<List<AppointmentResponse>> getAppointmentsByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(appointmentService.getAppointmentsByUser(userId));
    }

    @GetMapping("/history/doctor/{doctorId}")
    @Operation(summary = "Get appointment history for a doctor")
    public ResponseEntity<List<AppointmentResponse>> getAppointmentsByDoctor(@PathVariable Long doctorId) {
        return ResponseEntity.ok(appointmentService.getAppointmentsByDoctor(doctorId));
    }

    @GetMapping("/history/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all appointments across platform (Admin only)")
    public ResponseEntity<List<AppointmentResponse>> getAppointmentsAdmin() {
        return ResponseEntity.ok(appointmentService.getAllAppointmentsAdmin());
    }

    @PutMapping("/appointment/{appointmentId}")
    @Operation(summary = "Update appointment status (APPROVED, COMPLETED, CANCELLED)")
    public ResponseEntity<AppointmentResponse> updateAppointmentStatus(
            @PathVariable Long appointmentId,
            @RequestBody AppointmentStatusUpdateRequest request) {
        return ResponseEntity.ok(appointmentService.updateAppointmentStatus(appointmentId, request.getStatus()));
    }

    @DeleteMapping("/appointment/{appointmentId}")
    @Operation(summary = "Delete an appointment record")
    public ResponseEntity<Map<String, String>> deleteAppointment(@PathVariable Long appointmentId) {
        appointmentService.deleteAppointment(appointmentId);
        return ResponseEntity.ok(Map.of("message", "Appointment deleted successfully."));
    }

    @PutMapping("/approve/{doctorId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Admin approval of pending doctor profile")
    public ResponseEntity<Map<String, Object>> approveDoctor(@PathVariable Long doctorId) {
        DoctorProfileResponse response = doctorService.approveDoctor(doctorId);
        return ResponseEntity.ok(Map.of(
                "message", "Doctor approved successfully.",
                "doctor", response
        ));
    }

    @DeleteMapping("/{doctorId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Admin deletion of doctor profile and cascaded bookings")
    public ResponseEntity<Map<String, String>> deleteDoctor(@PathVariable Long doctorId) {
        doctorService.deleteDoctor(doctorId);
        return ResponseEntity.ok(Map.of("message", "Doctor profile deleted successfully."));
    }
}
