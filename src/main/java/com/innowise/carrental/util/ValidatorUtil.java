package com.innowise.carrental.util;

import com.innowise.carrental.exception.ValidationException;
import jakarta.servlet.http.Part;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Year;
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

    public static void validateCarUpdate(String make, String model, int year, BigDecimal price) throws ValidationException {
        notBlank(make, "Car make must not be empty");

        notBlank(model, "Car model must not be empty");

        int currentYear = Year.now().getValue();

        inRange(year, 1900, currentYear + 1,
                "Car year must be between 1900 and " + (currentYear + 1));

        positive(price, "Price per day must be greater than 0");
    }

    public static void validateDates(LocalDate dateFrom, LocalDate dateTo)
            throws ValidationException {
        isTrue(dateFrom != null && dateTo != null, "Dates must not be empty");
        isTrue(dateFrom.isBefore(dateTo), "Return date must be after pickup date");
        isTrue(!dateFrom.isBefore(LocalDate.now()), "Pickup date cannot be in the past");
    }

    public static void validateReview(int rating, String comment) throws ValidationException {
        inRange(rating, 1, 5, "Rating must be between 1 and 5");
        notBlank(comment, "Comment must not be empty");
        maxLength(comment, 1000, "Comment must not exceed 1000 characters");
    }

    public static void validateRegistration(String email, String password, String fullName)
            throws ValidationException {
        validateEmailFormat(email);
        minLength(password, 8, "Password must be at least 8 characters");
        notBlank(fullName, "Full name must not be empty");
    }

    public static void validateLogin(String email, String password) throws ValidationException {
        validateEmailFormat(email);
        notBlank(password, "Password must not be empty");
    }

    private static void validateEmailFormat(String email) throws ValidationException {
        notBlank(email, "Email must not be empty");
        matchesEmail(email, "Invalid email format");
    }

    public static void isTrue(boolean condition, String message) throws ValidationException {
        if (!condition) {
            throw new ValidationException(message);
        }
    }

    // Returns a readable error message for the first invalid part, or null if all are fine.
    public static String validateImageParts(List<Part> parts, Set<String> allowedExtensions, long maxFileSizeBytes) {
        for (Part part : parts) {
            if (part.getSize() > maxFileSizeBytes) {
                return "File too large: max " + (maxFileSizeBytes / (1024 * 1024)) + " MB";
            }
            String extension = ParseUtil.extractExtension(part.getSubmittedFileName());
            if (!allowedExtensions.contains(extension)) {
                return "Invalid file type. Allowed: " + String.join(", ", allowedExtensions);
            }
        }
        return null;
    }

}
