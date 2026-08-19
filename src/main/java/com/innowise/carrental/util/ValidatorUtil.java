package com.innowise.carrental.util;

import com.innowise.carrental.exception.ValidationException;
import jakarta.servlet.http.Part;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

public final class ValidatorUtil {

    private ValidatorUtil() {
    }

    public static void notBlank(String value, String message) throws ValidationException {
        if (value == null || value.isBlank()) {
            throw new ValidationException(message);
        }
    }

    public static void minLength(String value, int min, String message) throws ValidationException {
        if (value == null || value.length() < min) {
            throw new ValidationException(message);
        }
    }

    public static void maxLength(String value, int max, String message) throws ValidationException {
        if (value != null && value.length() > max) {
            throw new ValidationException(message);
        }
    }

    public static void matchesEmail(String value, String message) throws ValidationException {
        if (value == null || !value.contains("@") || !value.contains(".")) {
            throw new ValidationException(message);
        }
    }

    public static void inRange(int value, int min, int max, String message) throws ValidationException {
        if (value < min || value > max) {
            throw new ValidationException(message);
        }
    }

    public static void positive(BigDecimal value, String message) throws ValidationException {
        if (value == null || value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException(message);
        }
    }

    public static void isTrue(boolean condition, String message) throws ValidationException {
        if (!condition) {
            throw new ValidationException(message);
        }
    }

    // Returns a readable error message for the first invalid part, or null if all are fine.
    public static String validateImageParts(List<Part> parts, Set<String> allowedExtensions, long maxFileSize) {
        for (Part part : parts) {
            if (part.getSize() > maxFileSize) {
                return "File too large: max " + (maxFileSize / (1024 * 1024)) + " MB";
            }
            String extension = ParseUtil.extractExtension(part.getSubmittedFileName());
            if (!allowedExtensions.contains(extension)) {
                return "Invalid file type. Allowed: " + String.join(", ", allowedExtensions);
            }
        }
        return null;
    }

}
