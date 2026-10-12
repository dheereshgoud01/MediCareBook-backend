package com.medicarebook.service;

import com.medicarebook.dto.DoctorApplyRequest;
import com.medicarebook.dto.DoctorProfileResponse;
import com.medicarebook.dto.DoctorProfileUpdateRequest;
import com.medicarebook.entity.DoctorProfile;
import com.medicarebook.entity.DoctorStatus;
import com.medicarebook.entity.Role;
import com.medicarebook.entity.User;
import com.medicarebook.exception.ResourceNotFoundException;
import com.medicarebook.repository.AppointmentRepository;
import com.medicarebook.repository.DoctorRepository;
import com.medicarebook.repository.UserRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;
    private final AppointmentRepository appointmentRepository;
    private final PasswordEncoder passwordEncoder;

    public DoctorService(DoctorRepository doctorRepository,
                         UserRepository userRepository,
                         AppointmentRepository appointmentRepository,
                         PasswordEncoder passwordEncoder) {
        this.doctorRepository = doctorRepository;
        this.userRepository = userRepository;
        this.appointmentRepository = appointmentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    @CacheEvict(value = "doctors", allEntries = true)
    public DoctorProfileResponse applyDoctor(DoctorApplyRequest request) {
        User user;

        if (request.getUserId() != null) {
            user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + request.getUserId()));
        } else {
            // Find user by name or create placeholder user
            user = userRepository.findByEmail(request.getName().toLowerCase().replaceAll("\\s+", "") + "@doctor.com")
                    .orElseGet(() -> {
                        User newUser = new User(
                                request.getName(),
                                request.getName().toLowerCase().replaceAll("\\s+", "") + "@doctor.com",
                                passwordEncoder.encode("123456"),
                                Role.DOCTOR
                        );
                        return userRepository.save(newUser);
                    });
        }

        // Elevate user role to DOCTOR
        if (user.getRole() != Role.DOCTOR && user.getRole() != Role.ADMIN) {
            user.setRole(Role.DOCTOR);
            userRepository.save(user);
        }

        DoctorProfile profile = doctorRepository.findByUserId(user.getId())
                .orElse(new DoctorProfile());

        profile.setUser(user);
        profile.setName(request.getName());
        profile.setSpecialization(request.getSpecialization());
        profile.setAvailability(request.getAvailability());
        profile.setFees(request.getFees());
        profile.setStatus(DoctorStatus.PENDING);

        DoctorProfile saved = doctorRepository.save(profile);
        return new DoctorProfileResponse(saved);
    }

    @Cacheable(value = "doctors")
    public List<DoctorProfileResponse> listDoctors() {
        return doctorRepository.findAll().stream()
                .map(DoctorProfileResponse::new)
                .collect(Collectors.toList());
    }

    public List<DoctorProfileResponse> listApprovedDoctors() {
        return doctorRepository.findByStatus(DoctorStatus.APPROVED).stream()
                .map(DoctorProfileResponse::new)
                .collect(Collectors.toList());
    }

    public DoctorProfileResponse getDoctorProfileByUserId(Long userId) {
        DoctorProfile profile = doctorRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor profile not found for user ID: " + userId));
        return new DoctorProfileResponse(profile);
    }

    public DoctorProfileResponse getDoctorProfileById(Long doctorId) {
        DoctorProfile profile = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor profile not found with ID: " + doctorId));
        return new DoctorProfileResponse(profile);
    }

    @Transactional
    @CacheEvict(value = "doctors", allEntries = true)
    public DoctorProfileResponse updateDoctorProfile(Long doctorId, DoctorProfileUpdateRequest request) {
        DoctorProfile profile = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor profile not found with ID: " + doctorId));

        if (request.getSpecialization() != null && !request.getSpecialization().trim().isEmpty()) {
            profile.setSpecialization(request.getSpecialization().trim());
        }
        profile.setAvailability(request.getAvailability());
        profile.setFees(request.getFees());

        DoctorProfile updated = doctorRepository.save(profile);
        return new DoctorProfileResponse(updated);
    }

    @Transactional
    @CacheEvict(value = "doctors", allEntries = true)
    public DoctorProfileResponse approveDoctor(Long doctorId) {
        DoctorProfile profile = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor profile not found with ID: " + doctorId));

        profile.setStatus(DoctorStatus.APPROVED);
        DoctorProfile saved = doctorRepository.save(profile);

        // Ensure user account has DOCTOR role
        User user = profile.getUser();
        if (user != null && user.getRole() != Role.DOCTOR && user.getRole() != Role.ADMIN) {
            user.setRole(Role.DOCTOR);
            userRepository.save(user);
        }

        return new DoctorProfileResponse(saved);
    }

    @Transactional
    @CacheEvict(value = "doctors", allEntries = true)
    public void deleteDoctor(Long doctorId) {
        DoctorProfile profile = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor profile not found with ID: " + doctorId));

        // Delete appointments for this doctor
        appointmentRepository.deleteByDoctorId(doctorId);

        // Demote user role back to PATIENT
        User user = profile.getUser();
        if (user != null && user.getRole() == Role.DOCTOR) {
            user.setRole(Role.PATIENT);
            userRepository.save(user);
        }

        doctorRepository.delete(profile);
    }
}
