package com.medicarebook.service;

import com.medicarebook.dto.AiChatRequest;
import com.medicarebook.dto.AiChatResponse;
import com.medicarebook.entity.DoctorProfile;
import com.medicarebook.entity.DoctorStatus;
import com.medicarebook.entity.Role;
import com.medicarebook.entity.User;
import com.medicarebook.repository.ChatMessageRepository;
import com.medicarebook.repository.DoctorRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AiAssistantServiceTest {

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private ChatMessageRepository chatMessageRepository;

    @InjectMocks
    private AiAssistantService aiAssistantService;

    @Test
    @DisplayName("Should detect Cardiology specialty when patient mentions chest pain")
    void testDetectCardiologySpecialty() {
        AiChatRequest request = new AiChatRequest("I am experiencing severe chest pain and palpitations", 1L);

        User docUser = new User("Dr. Priya Sharma", "priya@test.com", "pass", Role.DOCTOR);
        DoctorProfile doc = new DoctorProfile(docUser, "Dr. Priya Sharma", "Cardiology", "Mon-Fri 9AM-5PM", 800.0, DoctorStatus.APPROVED);

        when(doctorRepository.findByStatus(DoctorStatus.APPROVED)).thenReturn(List.of(doc));

        AiChatResponse response = aiAssistantService.processMessage(request);

        assertNotNull(response);
        assertEquals("Cardiology", response.getSuggestedSpecialty());
        assertTrue(response.getReply().contains("Cardiology"));
        assertFalse(response.getMatchingDoctors().isEmpty());
        assertTrue(response.getMatchingDoctors().get(0).contains("Dr. Priya Sharma"));
    }

    @Test
    @DisplayName("Should detect Dermatology specialty when patient mentions skin rash")
    void testDetectDermatologySpecialty() {
        AiChatRequest request = new AiChatRequest("I have a red itching skin rash on my arms", 2L);

        User docUser = new User("Dr. Arjun Mehta", "arjun@test.com", "pass", Role.DOCTOR);
        DoctorProfile doc = new DoctorProfile(docUser, "Dr. Arjun Mehta", "Dermatology", "Mon-Sat 10AM-4PM", 600.0, DoctorStatus.APPROVED);

        when(doctorRepository.findByStatus(DoctorStatus.APPROVED)).thenReturn(List.of(doc));

        AiChatResponse response = aiAssistantService.processMessage(request);

        assertNotNull(response);
        assertEquals("Dermatology", response.getSuggestedSpecialty());
        assertTrue(response.getReply().contains("Dermatology"));
    }

    @Test
    @DisplayName("Should provide grounded platform knowledge for booking questions")
    void testProvidePlatformKnowledge() {
        AiChatRequest request = new AiChatRequest("What is the booking policy?", 3L);

        AiChatResponse response = aiAssistantService.processMessage(request);

        assertNotNull(response);
        assertTrue(response.getReply().contains("Booking Policy"));
    }
}
