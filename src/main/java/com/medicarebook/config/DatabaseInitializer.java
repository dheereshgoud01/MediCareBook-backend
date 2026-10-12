package com.medicarebook.config;

import com.medicarebook.entity.DoctorProfile;
import com.medicarebook.entity.DoctorStatus;
import com.medicarebook.entity.Role;
import com.medicarebook.entity.User;
import com.medicarebook.repository.DoctorRepository;
import com.medicarebook.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DatabaseInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DatabaseInitializer.class);

    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;
    private final PasswordEncoder passwordEncoder;

    public DatabaseInitializer(UserRepository userRepository,
                               DoctorRepository doctorRepository,
                               PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.doctorRepository = doctorRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // Seed default Admin if not present
        if (!userRepository.existsByEmail("admin@test.com")) {
            User admin = new User(
                    "System Administrator",
                    "admin@test.com",
                    passwordEncoder.encode("admin123"),
                    Role.ADMIN
            );
            userRepository.save(admin);
            log.info("Initialized default Administrator: admin@test.com (password: admin123)");
        }

        // Seed sample Doctor 1 (Cardiology) if not present
        if (!userRepository.existsByEmail("priya@test.com")) {
            User doctorUser = new User(
                    "Dr. Priya Sharma",
                    "priya@test.com",
                    passwordEncoder.encode("doctor123"),
                    Role.DOCTOR
            );
            User savedDocUser = userRepository.save(doctorUser);

            DoctorProfile profile = new DoctorProfile(
                    savedDocUser,
                    "Dr. Priya Sharma",
                    "Cardiology",
                    "Mon-Fri 9AM-5PM",
                    800.0,
                    DoctorStatus.APPROVED
            );
            doctorRepository.save(profile);
            log.info("Initialized sample Doctor: Dr. Priya Sharma (Cardiology)");
        }

        // Seed sample Doctor 2 (Dermatology) if not present
        if (!userRepository.existsByEmail("arjun@test.com")) {
            User doctorUser2 = new User(
                    "Dr. Arjun Mehta",
                    "arjun@test.com",
                    passwordEncoder.encode("doctor123"),
                    Role.DOCTOR
            );
            User savedDocUser2 = userRepository.save(doctorUser2);

            DoctorProfile profile2 = new DoctorProfile(
                    savedDocUser2,
                    "Dr. Arjun Mehta",
                    "Dermatology",
                    "Mon-Sat 10AM-4PM",
                    600.0,
                    DoctorStatus.APPROVED
            );
            doctorRepository.save(profile2);
            log.info("Initialized sample Doctor: Dr. Arjun Mehta (Dermatology)");
        }

        // Seed sample Patient if not present
        if (!userRepository.existsByEmail("john@test.com")) {
            User patient = new User(
                    "John Doe",
                    "john@test.com",
                    passwordEncoder.encode("patient123"),
                    Role.PATIENT
            );
            userRepository.save(patient);
            log.info("Initialized sample Patient: john@test.com (password: patient123)");
        }
    }
}
