package com.medicarebook.entity;

public enum AppointmentStatus {
    PENDING,
    APPROVED,
    COMPLETED,
    CANCELLED;

    public static AppointmentStatus fromString(String statusStr) {
        if (statusStr == null) return PENDING;
        String s = statusStr.trim().toUpperCase();
        try {
            return AppointmentStatus.valueOf(s);
        } catch (IllegalArgumentException e) {
            return PENDING;
        }
    }
}
