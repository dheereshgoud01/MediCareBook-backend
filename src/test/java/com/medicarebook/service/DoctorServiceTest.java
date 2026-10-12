package com.medicarebook.service;

import com.medicarebook.dto.DoctorApplyRequest;
import com.medicarebook.dto.DoctorProfileResponse;
import com.medicarebook.dto.DoctorProfileUpdateRequest;
import com.medicarebook.entity.DoctorProfile;
import com.medicarebook.entity.DoctorStatus;
import com.medicarebook.entity.Role;
import com.medicarebook.entity.User;
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
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DoctorServiceTest {

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private DoctorService doctorService;

    private User docUser;
    private DoctorProfile profile;

    @BeforeEach
    void setUp() {
        docUser = new User("Dr. Priya Sharma", "priya@test.com", "pass", Role.DOCTOR);
        docUser.setId(20L);

        profile = new DoctorProfile(docUser, "Dr. Priya Sharma", "Cardiology", "Mon-Fri 9AM-5PM", 800.0, DoctorStatus.PENDING);
        profile.setId(200L);
    }

    @Test
    @DisplayName("Should submit doctor application and set status to PENDING")
    void testApplyDoctor() {
        DoctorApplyRequest request = new DoctorApplyRequest(20L, "Dr. Priya Sharma", "Cardiology", "Mon-Fri 9AM-5PM", 800.0);

        when(userRepository.findById(20L)).thenReturn(Optional.of(docUser));
        when(doctorRepository.findByUserId(20L)).thenReturn(Optional.empty());
        when(doctorRepository.save(any(DoctorProfile.class))).thenReturn(profile);

        DoctorProfileResponse response = doctorService.applyDoctor(request);

        assertNotNull(response);
        assertEquals("pending", response.getStatus());
        assertEquals("Cardiology", response.getSpecialization());
    }

    @Test
    @DisplayName("Should approve doctor profile and ensure DOCTOR role")
    void testApproveDoctor() {
        when(doctorRepository.findById(200L)).thenReturn(Optional.of(profile));
        when(doctorRepository.save(any(DoctorProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DoctorProfileResponse response = doctorService.approveDoctor(200L);

        assertEquals("approved", response.getStatus());
        assertEquals(DoctorStatus.APPROVED, profile.getStatus());
    }

    @Test
    @DisplayName("Should update fees, availability schedule, and specialization")
    void testUpdateDoctorProfile() {
        DoctorProfileUpdateRequest request = new DoctorProfileUpdateRequest("Mon-Sat 10AM-6PM", 950.0, "Dermatology");

        when(doctorRepository.findById(200L)).thenReturn(Optional.of(profile));
        when(doctorRepository.save(any(DoctorProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DoctorProfileResponse response = doctorService.updateDoctorProfile(200L, request);

        assertEquals("Mon-Sat 10AM-6PM", response.getAvailability());
        assertEquals(950.0, response.getFees());
        assertEquals("Dermatology", response.getSpecialization());
    }
}
