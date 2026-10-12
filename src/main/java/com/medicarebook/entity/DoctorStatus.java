package com.medicarebook.entity;

public enum DoctorStatus {
    PENDING,
    APPROVED,
    REJECTED;

    public static DoctorStatus fromString(String statusStr) {
        if (statusStr == null) return PENDING;
        String s = statusStr.trim().toUpperCase();
        try {
            return DoctorStatus.valueOf(s);
        } catch (IllegalArgumentException e) {
            return PENDING;
        }
    }
}
