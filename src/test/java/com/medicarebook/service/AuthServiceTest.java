package com.medicarebook.service;

import com.medicarebook.dto.AuthRequest;
import com.medicarebook.dto.AuthResponse;
import com.medicarebook.dto.RegisterRequest;
import com.medicarebook.entity.Role;
import com.medicarebook.entity.User;
import com.medicarebook.exception.BadRequestException;
import com.medicarebook.repository.AppointmentRepository;
import com.medicarebook.repository.DoctorRepository;
import com.medicarebook.repository.UserRepository;
import com.medicarebook.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider tokenProvider;

    @InjectMocks
    private AuthService authService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = new User("Alice Smith", "alice@test.com", "encodedPassword", Role.PATIENT);
        sampleUser.setId(10L);
    }

    @Test
    @DisplayName("Should successfully register patient and hash password")
    void testRegisterPatientSuccess() {
        RegisterRequest request = new RegisterRequest("Alice Smith", "alice@test.com", "plainPassword123", "user");

        when(userRepository.existsByEmail("alice@test.com")).thenReturn(false);
        when(passwordEncoder.encode("plainPassword123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(sampleUser);
        when(tokenProvider.generateTokenForUser(eq("alice@test.com"), eq(10L), eq("PATIENT"))).thenReturn("mock.jwt.token");

        AuthResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals("alice@test.com", response.getEmail());
        assertEquals("patient", response.getRole());
        assertEquals("mock.jwt.token", response.getToken());
        verify(passwordEncoder, times(1)).encode("plainPassword123");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Should reject registration when email already exists")
    void testRegisterDuplicateEmailThrowsException() {
        RegisterRequest request = new RegisterRequest("Alice Smith", "alice@test.com", "password", "user");
        when(userRepository.existsByEmail("alice@test.com")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> authService.register(request));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should successfully register administrator when admin role is selected")
    void testRegisterAdminSuccess() {
        RegisterRequest request = new RegisterRequest("System Admin", "sysadmin@test.com", "pass123", "admin");

        when(userRepository.existsByEmail("sysadmin@test.com")).thenReturn(false);
        when(passwordEncoder.encode("pass123")).thenReturn("encodedPass");

        User savedUser = new User("System Admin", "sysadmin@test.com", "encodedPass", Role.ADMIN);
        savedUser.setId(99L);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(tokenProvider.generateTokenForUser(eq("sysadmin@test.com"), eq(99L), eq("ADMIN"))).thenReturn("admin.token");

        AuthResponse response = authService.register(request);

        assertEquals("admin", response.getRole());
        assertEquals("admin.token", response.getToken());
    }

    @Test
    @DisplayName("Should register doctor with custom selected specialization")
    void testRegisterDoctorWithSpecialization() {
        RegisterRequest request = new RegisterRequest("Dr. Sanjay", "sanjay@test.com", "pass123", "doctor", "Neurology");

        when(userRepository.existsByEmail("sanjay@test.com")).thenReturn(false);
        when(passwordEncoder.encode("pass123")).thenReturn("encodedPass");

        User savedUser = new User("Dr. Sanjay", "sanjay@test.com", "encodedPass", Role.DOCTOR);
        savedUser.setId(88L);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(tokenProvider.generateTokenForUser(eq("sanjay@test.com"), eq(88L), eq("DOCTOR"))).thenReturn("doc.token");

        AuthResponse response = authService.register(request);

        assertEquals("doctor", response.getRole());
        verify(doctorRepository, times(1)).save(argThat(profile -> "Neurology".equals(profile.getSpecialization())));
    }

    @Test
    @DisplayName("Should login successfully with valid credentials")
    void testLoginSuccess() {
        AuthRequest request = new AuthRequest("alice@test.com", "plainPassword123");
        Authentication auth = mock(Authentication.class);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(auth);
        when(userRepository.findByEmail("alice@test.com")).thenReturn(Optional.of(sampleUser));
        when(tokenProvider.generateToken(auth, 10L)).thenReturn("valid.jwt.token");

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("alice@test.com", response.getEmail());
        assertEquals("valid.jwt.token", response.getToken());
    }

    @Test
    @DisplayName("Should throw exception when authentication fails on login")
    void testLoginBadCredentials() {
        AuthRequest request = new AuthRequest("alice@test.com", "wrongPassword");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThrows(BadCredentialsException.class, () -> authService.login(request));
    }
}
