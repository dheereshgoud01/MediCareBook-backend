package com.medicarebook.dto;

import java.util.List;

public class AiChatResponse {

    private String reply;
    private String suggestedSpecialty;
    private List<String> matchingDoctors;
    private String source; // "RAG_KNOWLEDGE_BASE" or "LLM_SERVICE" or "FALLBACK_ASSISTANT"

    public AiChatResponse() {
    }

    public AiChatResponse(String reply, String suggestedSpecialty, List<String> matchingDoctors, String source) {
        this.reply = reply;
        this.suggestedSpecialty = suggestedSpecialty;
        this.matchingDoctors = matchingDoctors;
        this.source = source;
    }

    public String getReply() {
        return reply;
    }

    public void setReply(String reply) {
        this.reply = reply;
    }

    public String getSuggestedSpecialty() {
        return suggestedSpecialty;
    }

    public void setSuggestedSpecialty(String suggestedSpecialty) {
        this.suggestedSpecialty = suggestedSpecialty;
    }

    public List<String> getMatchingDoctors() {
        return matchingDoctors;
    }

    public void setMatchingDoctors(List<String> matchingDoctors) {
        this.matchingDoctors = matchingDoctors;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
}
