package com.gdg.poppet.user.domain.enums;

public enum Gender {
    MALE, FEMALE;

    public static Gender fromString(String value) {
        if (value == null) {
            throw new IllegalArgumentException("gender cannot be null");
        }

        switch (value.trim().toLowerCase()) {
            case "male":
                return MALE;
            case "female":
                return FEMALE;
            default:
                throw new IllegalArgumentException("Invalid gender value: " + value);
        }
    }
}
