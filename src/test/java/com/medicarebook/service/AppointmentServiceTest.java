package com.medicarebook.service;

import com.medicarebook.dto.AppointmentBookRequest;
import com.medicarebook.dto.AppointmentResponse;
import com.medicarebook.entity.*;
import com.medicarebook.exception.ConflictException;
import com.medicarebook.exception.ResourceNotFoundException;
import com.medicarebook.repository.AppointmentRepository;
import com.medicarebook.repository.DoctorRepository;
import com.medicarebook.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AppointmentService appointmentService;

    private User patient;
    private DoctorProfile doctor;

    @BeforeEach
    void setUp() {
        patient = new User("John Doe", "john@test.com", "pass", Role.PATIENT);
        patient.setId(1L);

        User docUser = new User("Dr. Priya Sharma", "priya@test.com", "pass", Role.DOCTOR);
        docUser.setId(2L);

        doctor = new DoctorProfile(docUser, "Dr. Priya Sharma", "Cardiology", "Mon-Fri 9AM-5PM", 500.0, DoctorStatus.APPROVED);
        doctor.setId(5L);
    }

    @Test
    @DisplayName("Should successfully book appointment when slot is available")
    void testBookAppointmentSuccess() {
        AppointmentBookRequest request = new AppointmentBookRequest(
                1L, 5L, "John Doe", "Dr. Priya Sharma", "2026-10-20", "10:00 AM"
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(doctorRepository.findById(5L)).thenReturn(Optional.of(doctor));
        when(appointmentRepository.existsByDoctorSlotConflict(eq(5L), eq(LocalDate.parse("2026-10-20")), eq("10:00 AM"), eq(AppointmentStatus.CANCELLED)))
                .thenReturn(false);

        Appointment savedAppointment = new Appointment(
                patient, doctor, "John Doe", "Dr. Priya Sharma",
                LocalDate.parse("2026-10-20"), "10:00 AM", AppointmentStatus.PENDING
        );
        savedAppointment.setId(101L);

        when(appointmentRepository.save(any(Appointment.class))).thenReturn(savedAppointment);

        AppointmentResponse response = appointmentService.bookAppointment(request);

        assertNotNull(response);
        assertEquals(101L, response.getId());
        assertEquals("pending", response.getStatus());
        assertEquals("10:00 AM", response.getTime());
        verify(appointmentRepository, times(1)).save(any(Appointment.class));
    }

    @Test
    @DisplayName("Should reject booking and throw ConflictException when slot is already booked")
    void testBookAppointmentSlotConflictThrowsException() {
        AppointmentBookRequest request = new AppointmentBookRequest(
                1L, 5L, "John Doe", "Dr. Priya Sharma", "2026-10-20", "10:00 AM"
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(doctorRepository.findById(5L)).thenReturn(Optional.of(doctor));
        when(appointmentRepository.existsByDoctorSlotConflict(eq(5L), eq(LocalDate.parse("2026-10-20")), eq("10:00 AM"), eq(AppointmentStatus.CANCELLED)))
                .thenReturn(true);

        assertThrows(ConflictException.class, () -> appointmentService.bookAppointment(request));
        verify(appointmentRepository, never()).save(any(Appointment.class));
    }

    @Test
    @DisplayName("Should update appointment status correctly")
    void testUpdateAppointmentStatus() {
        Appointment appt = new Appointment(patient, doctor, "John", "Dr. Priya", LocalDate.now(), "11:00 AM", AppointmentStatus.PENDING);
        appt.setId(50L);

        when(appointmentRepository.findById(50L)).thenReturn(Optional.of(appt));
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AppointmentResponse response = appointmentService.updateAppointmentStatus(50L, "approved");

        assertNotNull(response);
        assertEquals("approved", response.getStatus());
    }

    @Test
    @DisplayName("Should retrieve patient appointment history")
    void testGetAppointmentsByUser() {
        Appointment appt1 = new Appointment(patient, doctor, "John", "Dr. Priya", LocalDate.now(), "11:00 AM", AppointmentStatus.APPROVED);
        appt1.setId(1L);

        when(appointmentRepository.findByUserIdOrderByAppointmentDateDescCreatedAtDesc(1L))
                .thenReturn(List.of(appt1));

        List<AppointmentResponse> history = appointmentService.getAppointmentsByUser(1L);

        assertEquals(1, history.size());
        assertEquals("approved", history.get(0).getStatus());
    }
}
