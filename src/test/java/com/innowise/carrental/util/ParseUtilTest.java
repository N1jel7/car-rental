package com.innowise.carrental.util;

import com.innowise.carrental.exception.ValidationException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Part;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ParseUtilTest {

    // --- parseLong / parseInt / parseBigDecimal / parseDate

    @Test
    void parseLong_validNumber_returnsValue() throws Exception {
        // given / when
        long result = ParseUtil.parseLong(" 42 ", "invalid");

        // then
        assertEquals(42L, result);
    }

    @Test
    void parseLong_nonNumeric_throwsValidationException() {
        // given / when / then
        assertThrows(ValidationException.class, () -> ParseUtil.parseLong("not-a-number", "invalid car"));
    }

    @Test
    void parseLong_nullValue_throwsValidationException() {
        // given / when / then
        assertThrows(ValidationException.class, () -> ParseUtil.parseLong(null, "invalid car"));
    }

    @Test
    void parseInt_nonNumeric_throwsValidationException() {
        // given / when / then
        assertThrows(ValidationException.class, () -> ParseUtil.parseInt("abc", "invalid year"));
    }

    @Test
    void parseBigDecimal_validNumber_returnsValue() throws Exception {
        // given / when
        BigDecimal result = ParseUtil.parseBigDecimal("75.50", "invalid price");

        // then
        assertEquals(new BigDecimal("75.50"), result);
    }

    @Test
    void parseBigDecimal_malformedNumber_throwsValidationException() {
        // given / when / then
        assertThrows(ValidationException.class, () -> ParseUtil.parseBigDecimal("75,50", "invalid price"));
    }

    @Test
    void parseDate_validIsoDate_returnsValue() throws Exception {
        // given / when
        LocalDate result = ParseUtil.parseDate("2026-09-01", "invalid date");

        // then
        assertEquals(LocalDate.of(2026, 9, 1), result);
    }

    @Test
    void parseDate_malformedDate_throwsValidationException() {
        // given / when / then
        assertThrows(ValidationException.class, () -> ParseUtil.parseDate("01/09/2026", "invalid date"));
    }

    // --- parseNewCoverIndex / parseNewIndex / parseExistingId

    @Test
    void parseNewCoverIndex_validNewSelection_returnsIndex() {
        // given / when
        int result = ParseUtil.parseNewCoverIndex("new:2", 5);

        // then
        assertEquals(2, result);
    }

    @Test
    void parseNewCoverIndex_indexOutOfBounds_fallsBackToZero() {
        // given / when
        int result = ParseUtil.parseNewCoverIndex("new:9", 3);

        // then
        assertEquals(0, result);
    }

    @Test
    void parseNewCoverIndex_existingSelection_fallsBackToZero() {
        // given / when
        int result = ParseUtil.parseNewCoverIndex("existing:7", 3);

        // then
        assertEquals(0, result);
    }

    @Test
    void parseExistingId_validExistingSelection_returnsId() {
        // given / when
        Long result = ParseUtil.parseExistingId("existing:42");

        // then
        assertEquals(42L, result);
    }

    @Test
    void parseExistingId_newSelection_returnsNull() {
        // given / when
        Long result = ParseUtil.parseExistingId("new:0");

        // then
        assertNull(result);
    }

    // --- parseLongOrNull / parseLongList

    @Test
    void parseLongOrNull_malformedValue_returnsNull() {
        // given / when
        Long result = ParseUtil.parseLongOrNull("nope");

        // then
        assertNull(result);
    }

    @Test
    void parseLongList_mixOfValidAndInvalid_skipsInvalidEntries() {
        // given
        String[] values = {"1", "not-a-number", "3"};

        // when
        List<Long> result = ParseUtil.parseLongList(values);

        // then
        assertEquals(List.of(1L, 3L), result);
    }

    @Test
    void parseLongList_nullArray_returnsEmptyList() {
        // given / when
        List<Long> result = ParseUtil.parseLongList(null);

        // then
        assertTrue(result.isEmpty());
    }

    // --- parsePriceOrNull

    @Test
    void parsePriceOrNull_negativeValue_returnsNull() {
        // given / when
        BigDecimal result = ParseUtil.parsePriceOrNull("-10");

        // then
        assertNull(result);
    }

    @Test
    void parsePriceOrNull_blankValue_returnsNull() {
        // given / when
        BigDecimal result = ParseUtil.parsePriceOrNull("  ");

        // then
        assertNull(result);
    }

    @Test
    void parsePriceOrNull_validValue_returnsBigDecimal() {
        // given / when
        BigDecimal result = ParseUtil.parsePriceOrNull("99.90");

        // then
        assertEquals(new BigDecimal("99.90"), result);
    }

    // --- parseAvailability

    @Test
    void parseAvailability_available_returnsTrue() {
        // given / when / then
        assertEquals(Boolean.TRUE, ParseUtil.parseAvailability("available"));
    }

    @Test
    void parseAvailability_occupied_returnsFalse() {
        // given / when / then
        assertEquals(Boolean.FALSE, ParseUtil.parseAvailability("occupied"));
    }

    @Test
    void parseAvailability_unknownValue_returnsNull() {
        // given / when / then
        assertNull(ParseUtil.parseAvailability("whatever"));
    }

    // --- trimToNull

    @Test
    void trimToNull_blankOrNull_returnsNull() {
        // given / when / then
        assertNull(ParseUtil.trimToNull(null));
        assertNull(ParseUtil.trimToNull("   "));
    }

    @Test
    void trimToNull_valueWithSurroundingSpaces_returnsTrimmed() {
        // given / when / then
        assertEquals("Toyota", ParseUtil.trimToNull("  Toyota  "));
    }

    // --- extractExtension

    @Test
    void extractExtension_filenameWithExtension_returnsLowercasedExtension() {
        // given / when / then
        assertEquals("jpg", ParseUtil.extractExtension("Photo.JPG"));
    }

    @Test
    void extractExtension_filenameWithoutExtension_returnsEmptyString() {
        // given / when / then
        assertEquals("", ParseUtil.extractExtension("noextension"));
    }

    @Test
    void extractExtension_nullFilename_returnsEmptyString() {
        // given / when / then
        assertEquals("", ParseUtil.extractExtension(null));
    }

    // --- collectParts

    @Test
    void collectParts_mixOfFieldsAndEmptyParts_returnsOnlyMatchingNonEmptyParts() throws Exception {
        // given
        Part matchingPart = mock(Part.class);
        when(matchingPart.getName()).thenReturn("images");
        when(matchingPart.getSize()).thenReturn(1024L);

        Part emptyPart = mock(Part.class);
        when(emptyPart.getName()).thenReturn("images");
        when(emptyPart.getSize()).thenReturn(0L);

        Part otherFieldPart = mock(Part.class);
        when(otherFieldPart.getName()).thenReturn("description");

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getParts()).thenReturn(List.of(matchingPart, emptyPart, otherFieldPart));

        // when
        List<Part> result = ParseUtil.collectParts(request, "images");

        // then
        assertEquals(List.of(matchingPart), result);
    }

    @Test
    void collectParts_noParts_returnsEmptyList() throws Exception {
        // given
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getParts()).thenReturn(Collections.emptyList());

        // when
        List<Part> result = ParseUtil.collectParts(request, "images");

        // then
        assertTrue(result.isEmpty());
    }

}
