package com.medicarebook.entity;

public enum Role {
    PATIENT,
    DOCTOR,
    ADMIN;

    public static Role fromString(String roleStr) {
        if (roleStr == null) return PATIENT;
        String normalized = roleStr.trim().toUpperCase();
        if (normalized.equals("USER") || normalized.equals("PATIENT") || normalized.equals("ROLE_PATIENT") || normalized.equals("ROLE_USER")) {
            return PATIENT;
        } else if (normalized.equals("DOCTOR") || normalized.equals("ROLE_DOCTOR")) {
            return DOCTOR;
        } else if (normalized.equals("ADMIN") || normalized.equals("ROLE_ADMIN")) {
            return ADMIN;
        }
        return PATIENT;
    }
}
