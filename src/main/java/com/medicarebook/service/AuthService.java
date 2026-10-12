package com.medicarebook.service;

import com.medicarebook.dto.*;
import com.medicarebook.entity.DoctorProfile;
import com.medicarebook.entity.DoctorStatus;
import com.medicarebook.entity.Role;
import com.medicarebook.entity.User;
import com.medicarebook.exception.BadRequestException;
import com.medicarebook.exception.ResourceNotFoundException;
import com.medicarebook.repository.AppointmentRepository;
import com.medicarebook.repository.DoctorRepository;
import com.medicarebook.repository.UserRepository;
import com.medicarebook.security.JwtTokenProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    public AuthService(UserRepository userRepository,
                       DoctorRepository doctorRepository,
                       AppointmentRepository appointmentRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtTokenProvider tokenProvider) {
        this.userRepository = userRepository;
        this.doctorRepository = doctorRepository;
        this.appointmentRepository = appointmentRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("User with this email already exists: " + request.getEmail());
        }

        Role assignedRole = Role.fromString(request.getRole());

        User user = new User(
                request.getName(),
                request.getEmail().toLowerCase().trim(),
                passwordEncoder.encode(request.getPassword()),
                assignedRole
        );

        User savedUser = userRepository.save(user);

        // If registered as doctor, create initial pending doctor profile shell with selected specialization
        if (assignedRole == Role.DOCTOR) {
            String specialty = (request.getSpecialization() != null && !request.getSpecialization().trim().isEmpty())
                    ? request.getSpecialization().trim()
                    : "General Physician";

            DoctorProfile initialProfile = new DoctorProfile(
                    savedUser,
                    savedUser.getName(),
                    specialty,
                    "Mon-Fri 9AM-5PM",
                    50.0,
                    DoctorStatus.PENDING
            );
            doctorRepository.save(initialProfile);
        }

        String token = tokenProvider.generateTokenForUser(savedUser.getEmail(), savedUser.getId(), savedUser.getRole().name());

        return new AuthResponse(
                token,
                savedUser.getId(),
                savedUser.getName(),
                savedUser.getEmail(),
                savedUser.getRole().name()
        );
    }

    public AuthResponse login(AuthRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail().toLowerCase().trim(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail().toLowerCase().trim())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + request.getEmail()));

        String token = tokenProvider.generateToken(authentication, user.getId());

        return new AuthResponse(
                token,
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole().name()
        );
    }

    public List<UserResponse> listUsers() {
        return userRepository.findAll().stream()
                .map(UserResponse::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public UserResponse updateUser(Long userId, UserUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        if (request.getName() != null && !request.getName().isBlank()) {
            user.setName(request.getName());
        }
        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            user.setEmail(request.getEmail().toLowerCase().trim());
        }
        if (request.getRole() != null && !request.getRole().isBlank()) {
            user.setRole(Role.fromString(request.getRole()));
        }

        User updated = userRepository.save(user);
        return new UserResponse(updated);
    }

    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        // Cascading delete doctor profile if exists
        doctorRepository.findByUserId(userId).ifPresent(doctorProfile -> {
            appointmentRepository.deleteByDoctorId(doctorProfile.getId());
            doctorRepository.delete(doctorProfile);
        });

        // Delete user's patient appointments
        appointmentRepository.deleteByUserId(userId);

        // Delete user
        userRepository.delete(user);
    }
}
