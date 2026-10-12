package com.medicarebook.service;

import com.medicarebook.dto.AiChatRequest;
import com.medicarebook.dto.AiChatResponse;
import com.medicarebook.entity.ChatMessage;
import com.medicarebook.entity.DoctorProfile;
import com.medicarebook.entity.DoctorStatus;
import com.medicarebook.repository.ChatMessageRepository;
import com.medicarebook.repository.DoctorRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AiAssistantService {

    private final DoctorRepository doctorRepository;
    private final ChatMessageRepository chatMessageRepository;

    @Value("${app.ai.api-key:}")
    private String apiKey;

    public AiAssistantService(DoctorRepository doctorRepository, ChatMessageRepository chatMessageRepository) {
        this.doctorRepository = doctorRepository;
        this.chatMessageRepository = chatMessageRepository;
    }

    private static final Map<String, List<String>> SPECIALTY_KEYWORDS = new LinkedHashMap<>();

    static {
        SPECIALTY_KEYWORDS.put("Cardiology", Arrays.asList(
                "chest pain", "heart", "blood pressure", "palpitation", "cardio", "hypertension",
                "shortness of breath", "cholesterol", "angina", "cardiac", "pulse", "arrhythmia"
        ));
        SPECIALTY_KEYWORDS.put("Dermatology", Arrays.asList(
                "skin", "rash", "acne", "itching", "eczema", "hair fall", "dermatitis", "mole",
                "sunburn", "psoriasis", "dandruff", "pimples", "fungal", "pigmentation", "hives"
        ));
        SPECIALTY_KEYWORDS.put("Neurology", Arrays.asList(
                "headache", "migraine", "dizziness", "nerve", "seizure", "numbness", "tingling",
                "tremor", "memory", "vertigo", "concussion", "neuropathy", "fainting", "paralysis"
        ));
        SPECIALTY_KEYWORDS.put("Orthopedics", Arrays.asList(
                "bone", "joint", "fracture", "back pain", "knee", "spine", "arthritis", "sprain",
                "shoulder", "neck pain", "ligament", "muscle pain", "posture", "swollen joint", "hip pain"
        ));
        SPECIALTY_KEYWORDS.put("Pediatrics", Arrays.asList(
                "child", "baby", "infant", "toddler", "pediatric", "vaccine", "growth", "newborn",
                "childhood", "kids", "bedwetting", "colic"
        ));
        SPECIALTY_KEYWORDS.put("Psychiatry", Arrays.asList(
                "anxiety", "depression", "stress", "sleep", "insomnia", "mental health", "panic",
                "mood", "bipolar", "ptsd", "adhd", "burnout", "hallucination", "sadness"
        ));
        SPECIALTY_KEYWORDS.put("Gynecology", Arrays.asList(
                "pregnancy", "period", "menstrual", "gynec", "fertility", "uterus", "pcos", "pcod",
                "cramps", "ovary", "vaginal", "hormonal", "menopause"
        ));
        SPECIALTY_KEYWORDS.put("Ophthalmology", Arrays.asList(
                "eye", "vision", "blur", "blurry", "cataract", "dry eyes", "eyesight", "spectacle",
                "cornea", "glaucoma", "retina", "eye strain", "double vision"
        ));
        SPECIALTY_KEYWORDS.put("ENT (Ear, Nose & Throat)", Arrays.asList(
                "ear", "throat", "sinus", "tonsil", "hearing", "earwax", "tinnitus", "nasal",
                "hoarseness", "sore throat", "smell", "runny nose", "snoring", "deviated septum"
        ));
        SPECIALTY_KEYWORDS.put("Dentistry", Arrays.asList(
                "tooth", "teeth", "gum", "dental", "cavity", "toothache", "bleeding gum",
                "root canal", "wisdom tooth", "enamel", "bad breath", "braces"
        ));
        SPECIALTY_KEYWORDS.put("General Surgery", Arrays.asList(
                "hernia", "appendix", "gallbladder", "cyst", "abscess", "wound", "biopsy",
                "surgery", "tumor", "piles", "fistula"
        ));
        SPECIALTY_KEYWORDS.put("General Physician", Arrays.asList(
                "fever", "cold", "cough", "flu", "weakness", "body ache", "vomiting", "stomach",
                "infection", "fatigue", "chills", "diarrhea", "nausea", "head cold", "indigestion"
        ));
    }

    public AiChatResponse processMessage(AiChatRequest request) {
        String prompt = (request.getMessage() != null) ? request.getMessage().trim() : "";
        String lowerPrompt = prompt.toLowerCase();

        StringBuilder reply = new StringBuilder();
        String detectedSpecialty = null;
        List<String> matchingDoctors = new ArrayList<>();
        String responseSource = "INTELLIGENT_KNOWLEDGE_ENGINE";

        // 1. Emergency Safety Intercept
        if (isEmergencyQuery(lowerPrompt)) {
            reply.append("🚨 **Emergency Medical Alert**\n\n")
                 .append("If you or someone near you is experiencing a medical emergency (such as severe chest pain, inability to breathe, sudden numbness, uncontrolled bleeding, or loss of consciousness), **do not wait for an online appointment**.\n\n")
                 .append("• **Emergency Services**: Dial **112 / 102 / 108** (India) or your local emergency number immediately.\n")
                 .append("• Please visit the nearest hospital emergency room without delay.");
            return finalizeResponse(request, prompt, reply.toString(), null, Collections.emptyList(), "EMERGENCY_OVERRIDE");
        }

        // 2. Available Doctors / Active Appointments Query
        if (isAvailableDoctorsQuery(lowerPrompt)) {
            List<DoctorProfile> approved = doctorRepository.findByStatus(DoctorStatus.APPROVED);
            if (approved.isEmpty()) {
                reply.append("Currently, there are no approved doctors listed in our directory. Please check back shortly or register as a medical doctor to be listed.");
            } else {
                reply.append("### 🩺 Currently Available Doctors on MediCareBook\n\n")
                     .append("The following verified practitioners have active schedules and are accepting consultations:\n\n");

                for (DoctorProfile d : approved) {
                    String docName = d.getName().startsWith("Dr.") ? d.getName() : "Dr. " + d.getName();
                    reply.append("• **").append(docName).append("** — *")
                         .append(d.getSpecialization()).append("*\n")
                         .append("  - **Fee**: ₹").append(d.getFees())
                         .append(" | **Hours**: ").append(d.getAvailability()).append("\n");
                }

                reply.append("\n📅 **How to book**: Go to your **Patient Dashboard**, select your chosen doctor from the directory, choose an open date, pick your preferred time slot, and click **Confirm booking**.");
            }
            return finalizeResponse(request, prompt, reply.toString(), null, matchingDoctors, "LIVE_DATABASE_LOOKUP");
        }

        // 3. Platform Workflow Inquiries (Precision RAG)
        if (matchesBookingInquiry(lowerPrompt)) {
            reply.append("### 📅 Appointment Booking Policy & Guide\n\n")
                 .append("1. **Log in** to your account (register as a Patient if you don't have an account).\n")
                 .append("2. In the **Patient Dashboard**, explore the **Doctor Directory** or filter by medical specialty.\n")
                 .append("3. Click on a doctor to select them.\n")
                 .append("4. Pick your preferred **Date** and **Time Slot** (e.g. `10:00 AM`).\n")
                 .append("5. Click **Confirm booking**.\n\n")
                 .append("🛡️ *Our relational database automatically enforces double-booking prevention to guarantee no two patients overlap on the same slot.*");
            return finalizeResponse(request, prompt, reply.toString(), null, matchingDoctors, "PLATFORM_KNOWLEDGE_BASE");
        }

        if (matchesCancellationInquiry(lowerPrompt)) {
            reply.append("### ❌ Appointment Cancellation Policy\n\n")
                 .append("• Patients can cancel their pending or approved consultations at any time directly from the **Appointment History** table by clicking **Cancel**.\n")
                 .append("• Practitioners and administrators also have authority to review and update appointment statuses (`APPROVED`, `COMPLETED`, `CANCELLED`).\n")
                 .append("• Once cancelled, the slot immediately becomes open again for other patients.");
            return finalizeResponse(request, prompt, reply.toString(), null, matchingDoctors, "PLATFORM_KNOWLEDGE_BASE");
        }

        if (matchesDoctorApplicationInquiry(lowerPrompt)) {
            reply.append("### 🩺 How to Apply as a Medical Doctor\n\n")
                 .append("1. **Sign up** on the registration page with the **Medical Doctor** account role and select your specialization.\n")
                 .append("2. Existing patients can also apply directly from the Patient Dashboard under **Apply as a doctor**.\n")
                 .append("3. Provide your professional name, specialty, availability days/hours, and consultation fee.\n")
                 .append("4. An **Administrator** will inspect your application in the approval queue and grant verified status. Once approved, your profile becomes publicly bookable!");
            return finalizeResponse(request, prompt, reply.toString(), null, matchingDoctors, "PLATFORM_KNOWLEDGE_BASE");
        }

        if (matchesFeesInquiry(lowerPrompt)) {
            reply.append("### 💳 Consultation Fees & Payment\n\n")
                 .append("• Each verified doctor sets their own transparent consultation fee (typically ₹400 – ₹1000) displayed on their profile card.\n")
                 .append("• The fee displayed on MediCareBook is for informational and scheduling purposes. Actual payments are settled during the physical consultation.\n")
                 .append("• The platform does not charge booking fees or hidden surcharges.");
            return finalizeResponse(request, prompt, reply.toString(), null, matchingDoctors, "PLATFORM_KNOWLEDGE_BASE");
        }

        if (matchesLifecycleInquiry(lowerPrompt)) {
            reply.append("### 🔄 Appointment Status Lifecycle\n\n")
                 .append("Every appointment follows a synchronized state machine:\n")
                 .append("1. **PENDING**: Initial state after patient booking; awaiting doctor review.\n")
                 .append("2. **APPROVED**: Doctor has confirmed the slot and reserved consultation time.\n")
                 .append("3. **COMPLETED**: The consultation visit was concluded.\n")
                 .append("4. **CANCELLED**: The booking was voided by the patient or doctor.");
            return finalizeResponse(request, prompt, reply.toString(), null, matchingDoctors, "PLATFORM_KNOWLEDGE_BASE");
        }

        if (matchesGreetings(lowerPrompt)) {
            reply.append("Hello! I am your **MediCareBook AI Healthcare Assistant**.\n\n")
                 .append("Here is how I can assist you:\n")
                 .append("• **Symptom Check**: Describe any symptoms (e.g., *fever and cough*, *skin rash*, *chest pain*), and I will recommend the right clinical specialty.\n")
                 .append("• **Find Doctors**: Ask *\"Which doctors are available?\"* to see practitioners accepting appointments right now.\n")
                 .append("• **Booking Help**: Ask about how to book, cancel, or apply as a doctor.\n\n")
                 .append("How can I help you today?");
            return finalizeResponse(request, prompt, reply.toString(), null, matchingDoctors, "CONVERSATIONAL_GREETING");
        }

        // 4. Clinical Symptom & Specialty Analysis
        int maxScore = 0;
        for (Map.Entry<String, List<String>> entry : SPECIALTY_KEYWORDS.entrySet()) {
            int score = 0;
            for (String kw : entry.getValue()) {
                if (lowerPrompt.contains(kw)) {
                    score++;
                }
            }
            if (score > maxScore) {
                maxScore = score;
                detectedSpecialty = entry.getKey();
            }
        }

        if (detectedSpecialty != null) {
            final String spec = detectedSpecialty;
            List<DoctorProfile> approved = doctorRepository.findByStatus(DoctorStatus.APPROVED);
            matchingDoctors = approved.stream()
                    .filter(d -> d.getSpecialization().toLowerCase().contains(spec.toLowerCase()) ||
                                 spec.toLowerCase().contains(d.getSpecialization().toLowerCase()))
                    .map(d -> (d.getName().startsWith("Dr.") ? d.getName() : "Dr. " + d.getName()) + 
                              " (" + d.getSpecialization() + " | ₹" + d.getFees() + " | " + d.getAvailability() + ")")
                    .collect(Collectors.toList());

            reply.append("Based on the symptoms and concerns you described, consulting a specialist in **")
                 .append(detectedSpecialty)
                 .append("** is recommended.\n\n");

            if (!matchingDoctors.isEmpty()) {
                reply.append("Here are verified practitioners on MediCareBook currently available in this field:\n\n");
                for (String docInfo : matchingDoctors) {
                    reply.append("• ").append(docInfo).append("\n");
                }
                reply.append("\nYou can book an appointment slot directly through your **Patient Dashboard**.");
            } else {
                reply.append("We currently do not have a dedicated practitioner in this specific specialty listed on the platform. However, our verified **General Physicians** can provide comprehensive primary evaluations and referrals.");
            }
            reply.append("\n\n*Medical Disclaimer: MediCareBook AI provides informational guidance only and does not replace professional medical diagnosis or clinical treatment.*");

            return finalizeResponse(request, prompt, reply.toString(), detectedSpecialty, matchingDoctors, "CLINICAL_SYMPTOM_ANALYSIS");
        }

        // 5. Default Helpful Fallback
        reply.append("I couldn't quite pinpoint a specific symptom or platform question from your message.\n\n")
             .append("Here are some things you can try:\n")
             .append("• Describe your physical symptoms (e.g., *\"I have a high fever with chills\"* or *\"My knee hurts when bending\"*)\n")
             .append("• Inquire about available practitioners (e.g., *\"Which doctors are available?\"*)\n")
             .append("• Ask about platform procedures (e.g., *\"How do I book an appointment?\"*)\n\n")
             .append("Please let me know how I can guide you!");

        return finalizeResponse(request, prompt, reply.toString(), null, matchingDoctors, "HELPFUL_FALLBACK");
    }

    private boolean isEmergencyQuery(String text) {
        return text.contains("emergency") || text.contains("ambulance") || text.contains("heart attack") ||
               text.contains("cannot breathe") || text.contains("unconscious") || text.contains("severe bleeding") ||
               text.contains("poison");
    }

    private boolean isAvailableDoctorsQuery(String text) {
        return (text.contains("available") && (text.contains("appointment") || text.contains("doctor") || text.contains("which") || text.contains("who") || text.contains("slot"))) ||
               text.contains("which appointments are") || text.contains("which doctors are") ||
               text.contains("who are the available doctors") || text.contains("show doctors") ||
               text.contains("list doctors") || text.contains("who is available") ||
               text.contains("available doctors") || text.contains("available appointments");
    }

    private boolean matchesBookingInquiry(String text) {
        return text.contains("how to book") || text.contains("how do i book") || text.contains("how can i book") ||
               text.contains("booking policy") || text.contains("book an appointment") ||
               text.contains("schedule an appointment") || text.contains("make an appointment");
    }

    private boolean matchesCancellationInquiry(String text) {
        return text.contains("cancel") || text.contains("cancellation") || text.contains("reschedule");
    }

    private boolean matchesDoctorApplicationInquiry(String text) {
        return text.contains("apply as doctor") || text.contains("become a doctor") ||
               text.contains("doctor registration") || text.contains("how to become doctor") ||
               text.contains("join as doctor") || text.contains("register as doctor");
    }

    private boolean matchesFeesInquiry(String text) {
        return text.contains("fee") || text.contains("pricing") || text.contains("cost") ||
               text.contains("charge") || text.contains("payment") || text.contains("how much");
    }

    private boolean matchesLifecycleInquiry(String text) {
        return text.contains("lifecycle") || text.contains("status pending") || text.contains("status approved");
    }

    private boolean matchesGreetings(String text) {
        return text.equals("hi") || text.equals("hello") || text.equals("hey") ||
               text.startsWith("hi ") || text.startsWith("hello ") || text.startsWith("hey ") ||
               text.contains("who are you") || text.contains("good morning") || text.contains("good afternoon") ||
               text.contains("good evening") || text.contains("what can you do");
    }

    private AiChatResponse finalizeResponse(AiChatRequest request, String prompt, String reply,
                                             String specialty, List<String> matchingDoctors, String source) {
        AiChatResponse response = new AiChatResponse(reply, specialty, matchingDoctors, source);
        try {
            ChatMessage chatMessage = new ChatMessage(
                    request.getUserId(),
                    "user@" + (request.getUserId() != null ? request.getUserId() : "guest"),
                    prompt,
                    reply
            );
            chatMessageRepository.save(chatMessage);
        } catch (Exception ignored) {
        }
        return response;
    }
}
