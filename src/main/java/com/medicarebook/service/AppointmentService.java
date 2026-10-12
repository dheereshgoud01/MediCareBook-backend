package com.medicarebook.service;

import com.medicarebook.dto.AppointmentBookRequest;
import com.medicarebook.dto.AppointmentResponse;
import com.medicarebook.entity.Appointment;
import com.medicarebook.entity.AppointmentStatus;
import com.medicarebook.entity.DoctorProfile;
import com.medicarebook.entity.User;
import com.medicarebook.exception.BadRequestException;
import com.medicarebook.exception.ConflictException;
import com.medicarebook.exception.ResourceNotFoundException;
import com.medicarebook.repository.AppointmentRepository;
import com.medicarebook.repository.DoctorRepository;
import com.medicarebook.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;

    public AppointmentService(AppointmentRepository appointmentRepository,
                              DoctorRepository doctorRepository,
                              UserRepository userRepository) {
        this.appointmentRepository = appointmentRepository;
        this.doctorRepository = doctorRepository;
        this.userRepository = userRepository;
    }

    private LocalDate parseDate(String rawDate) {
        if (rawDate == null || rawDate.isBlank()) {
            throw new BadRequestException("Appointment date must not be blank");
        }
        try {
            // Check if ISO timestamp e.g. "2026-10-15T00:00:00.000Z"
            if (rawDate.contains("T")) {
                return LocalDate.parse(rawDate.substring(0, 10));
            }
            return LocalDate.parse(rawDate, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (Exception e) {
            throw new BadRequestException("Invalid date format. Expected YYYY-MM-DD: " + rawDate);
        }
    }

    @Transactional
    public AppointmentResponse bookAppointment(AppointmentBookRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + request.getUserId()));

        DoctorProfile doctor = doctorRepository.findById(request.getDoctorId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with ID: " + request.getDoctorId()));

        LocalDate appointmentDate = parseDate(request.getDate());

        // Concurrency / double-booking check
        boolean slotBooked = appointmentRepository.existsByDoctorSlotConflict(
                doctor.getId(),
                appointmentDate,
                request.getTime().trim(),
                AppointmentStatus.CANCELLED
        );

        if (slotBooked) {
            throw new ConflictException("The doctor already has an active appointment on " + appointmentDate + " at " + request.getTime() + ". Please select another time slot.");
        }

        String patientName = (request.getUserName() != null && !request.getUserName().isBlank())
                ? request.getUserName()
                : user.getName();

        String docName = (request.getDoctorName() != null && !request.getDoctorName().isBlank())
                ? request.getDoctorName()
                : doctor.getName();

        Appointment appointment = new Appointment(
                user,
                doctor,
                patientName,
                docName,
                appointmentDate,
                request.getTime().trim(),
                AppointmentStatus.PENDING
        );
        appointment.setNotes(request.getNotes());

        Appointment saved = appointmentRepository.save(appointment);
        return new AppointmentResponse(saved);
    }

    public List<AppointmentResponse> getAppointmentsByUser(Long userId) {
        return appointmentRepository.findByUserIdOrderByAppointmentDateDescCreatedAtDesc(userId).stream()
                .map(AppointmentResponse::new)
                .collect(Collectors.toList());
    }

    public List<AppointmentResponse> getAppointmentsByDoctor(Long doctorId) {
        return appointmentRepository.findByDoctorIdOrderByAppointmentDateDescCreatedAtDesc(doctorId).stream()
                .map(AppointmentResponse::new)
                .collect(Collectors.toList());
    }

    public List<AppointmentResponse> getAllAppointmentsAdmin() {
        return appointmentRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(AppointmentResponse::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public AppointmentResponse updateAppointmentStatus(Long appointmentId, String newStatus) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with ID: " + appointmentId));

        AppointmentStatus status = AppointmentStatus.fromString(newStatus);
        appointment.setStatus(status);

        Appointment updated = appointmentRepository.save(appointment);
        return new AppointmentResponse(updated);
    }

    @Transactional
    public void deleteAppointment(Long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with ID: " + appointmentId));

        appointmentRepository.delete(appointment);
    }
}
