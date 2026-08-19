package com.innowise.carrental.util;

import com.innowise.carrental.exception.ValidationException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Part;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public final class ParseUtil {

    private static final String NEW_PREFIX = "new:";
    private static final String EXISTING_PREFIX = "existing:";

    private ParseUtil() {
    }

    public static long parseLong(String value, String errorMessage) throws ValidationException {
        try {
            return Long.parseLong(value.strip());
        } catch (NumberFormatException | NullPointerException e) {
            throw new ValidationException(errorMessage);
        }
    }

    public static int parseInt(String value, String errorMessage) throws ValidationException {
        try {
            return Integer.parseInt(value.strip());
        } catch (NumberFormatException | NullPointerException e) {
            throw new ValidationException(errorMessage);
        }
    }

    public static BigDecimal parseBigDecimal(String value, String errorMessage) throws ValidationException {
        try {
            return new BigDecimal(value.strip());
        } catch (NumberFormatException | NullPointerException e) {
            throw new ValidationException(errorMessage);
        }
    }

    public static LocalDate parseDate(String value, String errorMessage) throws ValidationException {
        try {
            return LocalDate.parse(value.strip());
        } catch (Exception e) {
            throw new ValidationException(errorMessage);
        }
    }

    public static int parseNewCoverIndex(String primarySelection, int uploadedCount) {
        Integer index = parseNewIndex(primarySelection);
        return (index != null && index >= 0 && index < uploadedCount) ? index : 0;
    }

    public static Integer parseNewIndex(String primarySelection) {
        if (primarySelection == null || !primarySelection.startsWith(NEW_PREFIX)) {
            return null;
        }
        try {
            return Integer.parseInt(primarySelection.substring(NEW_PREFIX.length()));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static Long parseExistingId(String primarySelection) {
        if (primarySelection == null || !primarySelection.startsWith(EXISTING_PREFIX)) {
            return null;
        }
        return parseLongOrNull(primarySelection.substring(EXISTING_PREFIX.length()));
    }

    public static Long parseLongOrNull(String value) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static List<Long> parseLongList(String[] values) {
        List<Long> result = new ArrayList<>();
        if (values == null) {
            return result;
        }
        for (String value : values) {
            Long id = parseLongOrNull(value);
            if (id != null) {
                result.add(id);
            }
        }
        return result;
    }

    public static BigDecimal parsePriceOrNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            BigDecimal price = new BigDecimal(value.strip());
            return price.signum() < 0 ? null : price;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // "available" -> only free cars, "occupied" -> only booked/unavailable, anything else -> no filter
    public static Boolean parseAvailability(String value) {
        if ("available".equals(value)) return true;
        if ("occupied".equals(value)) return false;
        return null;
    }

    public static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.strip();
        return trimmed.isEmpty() ? null : trimmed;
    }

    public static String extractExtension(String filename) {
        return filename != null && filename.contains(".")
                ? filename.substring(filename.lastIndexOf('.') + 1).toLowerCase()
                : "";
    }

    // Collects multipart file parts submitted under the given form field name, skipping empty ones.
    public static List<Part> collectParts(HttpServletRequest request, String fieldName)
            throws IOException, ServletException {
        List<Part> parts = new ArrayList<>();
        for (Part part : request.getParts()) {
            if (fieldName.equals(part.getName()) && part.getSize() > 0) {
                parts.add(part);
            }
        }
        return parts;
    }
}
