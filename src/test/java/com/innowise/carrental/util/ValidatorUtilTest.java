package com.innowise.carrental.util;

import com.innowise.carrental.exception.ValidationException;
import jakarta.servlet.http.Part;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Year;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ValidatorUtilTest {

    // --- notBlank

    @Test
    void notBlank_nullOrBlank_throwsValidationException() {
        // given / when / then
        assertThrows(ValidationException.class, () -> ValidatorUtil.notBlank(null, "required"));
        assertThrows(ValidationException.class, () -> ValidatorUtil.notBlank("   ", "required"));
    }

    @Test
    void notBlank_nonBlankValue_doesNotThrow() {
        // given
        String value = "Toyota";

        // when / then
        assertDoesNotThrow(() -> ValidatorUtil.notBlank(value, "required"));
    }

    // --- minLength / maxLength

    @Test
    void minLength_shorterThanMinimum_throwsValidationException() {
        // given / when / then
        assertThrows(ValidationException.class, () -> ValidatorUtil.minLength("1234567", 8, "too short"));
    }

    @Test
    void minLength_exactlyMinimum_doesNotThrow() {
        // given / when / then
        assertDoesNotThrow(() -> ValidatorUtil.minLength("12345678", 8, "too short"));
    }

    @Test
    void maxLength_longerThanMaximum_throwsValidationException() {
        // given
        String comment = "a".repeat(1001);

        // when / then
        assertThrows(ValidationException.class, () -> ValidatorUtil.maxLength(comment, 1000, "too long"));
    }

    @Test
    void maxLength_exactlyMaximum_doesNotThrow() {
        // given
        String comment = "a".repeat(1000);

        // when / then
        assertDoesNotThrow(() -> ValidatorUtil.maxLength(comment, 1000, "too long"));
    }

    // --- matchesEmail

    @Test
    void matchesEmail_missingAtOrDot_throwsValidationException() {
        // given / when / then
        assertThrows(ValidationException.class, () -> ValidatorUtil.matchesEmail("not-an-email", "bad format"));
        assertThrows(ValidationException.class, () -> ValidatorUtil.matchesEmail("user@nodot", "bad format"));
    }

    @Test
    void matchesEmail_validFormat_doesNotThrow() {
        // given / when / then
        assertDoesNotThrow(() -> ValidatorUtil.matchesEmail("user@carrental.com", "bad format"));
    }

    // --- inRange

    @Test
    void inRange_belowOrAboveBounds_throwsValidationException() {
        // given / when / then
        assertThrows(ValidationException.class, () -> ValidatorUtil.inRange(1899, 1900, 2026, "out of range"));
        assertThrows(ValidationException.class, () -> ValidatorUtil.inRange(2027, 1900, 2026, "out of range"));
    }

    @Test
    void inRange_atBounds_doesNotThrow() {
        // given / when / then
        assertDoesNotThrow(() -> ValidatorUtil.inRange(1900, 1900, 2026, "out of range"));
        assertDoesNotThrow(() -> ValidatorUtil.inRange(2026, 1900, 2026, "out of range"));
    }

    // --- positive

    @Test
    void positive_nullZeroOrNegative_throwsValidationException() {
        // given / when / then
        assertThrows(ValidationException.class, () -> ValidatorUtil.positive(null, "must be positive"));
        assertThrows(ValidationException.class, () -> ValidatorUtil.positive(BigDecimal.ZERO, "must be positive"));
        assertThrows(ValidationException.class, () -> ValidatorUtil.positive(new BigDecimal("-5"), "must be positive"));
    }

    @Test
    void positive_positiveValue_doesNotThrow() {
        // given / when / then
        assertDoesNotThrow(() -> ValidatorUtil.positive(new BigDecimal("0.01"), "must be positive"));
    }

    // --- validateCarUpdate

    @Test
    void validateCarUpdate_validFields_doesNotThrow() {
        // given / when / then
        assertDoesNotThrow(() ->
                ValidatorUtil.validateCarUpdate("Toyota", "Camry", 2022, new BigDecimal("75.00")));
    }

    @Test
    void validateCarUpdate_yearTooFarInTheFuture_throwsValidationException() {
        // given
        int tooFar = Year.now().getValue() + 2;

        // when / then
        assertThrows(ValidationException.class, () ->
                ValidatorUtil.validateCarUpdate("Toyota", "Camry", tooFar, new BigDecimal("75.00")));
    }

    @Test
    void validateCarUpdate_nonPositivePrice_throwsValidationException() {
        // given / when / then
        assertThrows(ValidationException.class, () ->
                ValidatorUtil.validateCarUpdate("Toyota", "Camry", 2022, BigDecimal.ZERO));
    }

    // --- validateDates

    @Test
    void validateDates_returnBeforePickup_throwsValidationException() {
        // given
        LocalDate from = LocalDate.now().plusDays(5);
        LocalDate to = LocalDate.now().plusDays(2);

        // when / then
        assertThrows(ValidationException.class, () -> ValidatorUtil.validateDates(from, to));
    }

    @Test
    void validateDates_pickupInThePast_throwsValidationException() {
        // given
        LocalDate from = LocalDate.now().minusDays(1);
        LocalDate to = LocalDate.now().plusDays(2);

        // when / then
        assertThrows(ValidationException.class, () -> ValidatorUtil.validateDates(from, to));
    }

    @Test
    void validateDates_validRange_doesNotThrow() {
        // given
        LocalDate from = LocalDate.now().plusDays(1);
        LocalDate to = LocalDate.now().plusDays(3);

        // when / then
        assertDoesNotThrow(() -> ValidatorUtil.validateDates(from, to));
    }

    // --- validateReview

    @Test
    void validateReview_ratingOutOfRange_throwsValidationException() {
        // given / when / then
        assertThrows(ValidationException.class, () -> ValidatorUtil.validateReview(0, "Great trip"));
        assertThrows(ValidationException.class, () -> ValidatorUtil.validateReview(6, "Great trip"));
    }

    @Test
    void validateReview_validRatingAndComment_doesNotThrow() {
        // given / when / then
        assertDoesNotThrow(() -> ValidatorUtil.validateReview(5, "Great trip"));
    }

    // --- validateRegistration / validateLogin

    @Test
    void validateRegistration_shortPassword_throwsValidationException() {
        // given / when / then
        assertThrows(ValidationException.class, () ->
                ValidatorUtil.validateRegistration("user@carrental.com", "short", "Ivan Ivanov"));
    }

    @Test
    void validateRegistration_validFields_doesNotThrow() {
        // given / when / then
        assertDoesNotThrow(() ->
                ValidatorUtil.validateRegistration("user@carrental.com", "password123", "Ivan Ivanov"));
    }

    @Test
    void validateLogin_blankPassword_throwsValidationException() {
        // given / when / then
        assertThrows(ValidationException.class, () ->
                ValidatorUtil.validateLogin("user@carrental.com", " "));
    }

    // --- isTrue

    @Test
    void isTrue_falseCondition_throwsValidationException() {
        // given / when / then
        assertThrows(ValidationException.class, () -> ValidatorUtil.isTrue(false, "condition failed"));
    }

    @Test
    void isTrue_trueCondition_doesNotThrow() {
        // given / when / then
        assertDoesNotThrow(() -> ValidatorUtil.isTrue(true, "condition failed"));
    }

    // --- validateImageParts

    @Test
    void validateImageParts_fileTooLarge_returnsErrorMessage() {
        // given
        Part oversized = mock(Part.class);
        when(oversized.getSize()).thenReturn(20L * 1024 * 1024);

        // when
        String result = ValidatorUtil.validateImageParts(
                List.of(oversized), Set.of("jpg", "png"), 10L * 1024 * 1024);

        // then
        assertNotNull(result);
    }

    @Test
    void validateImageParts_disallowedExtension_returnsErrorMessage() {
        // given
        Part part = mock(Part.class);
        when(part.getSize()).thenReturn(1024L);
        when(part.getSubmittedFileName()).thenReturn("photo.gif");

        // when
        String result = ValidatorUtil.validateImageParts(
                List.of(part), Set.of("jpg", "png"), 10L * 1024 * 1024);

        // then
        assertNotNull(result);
    }

    @Test
    void validateImageParts_allPartsValid_returnsNull() {
        // given
        Part part = mock(Part.class);
        when(part.getSize()).thenReturn(1024L);
        when(part.getSubmittedFileName()).thenReturn("photo.jpg");

        // when
        String result = ValidatorUtil.validateImageParts(
                List.of(part), Set.of("jpg", "png"), 10L * 1024 * 1024);

        // then
        assertNull(result);
    }

}
