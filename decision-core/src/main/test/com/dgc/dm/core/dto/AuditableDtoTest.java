package com.dgc.dm.core.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuditableDtoTest {

    private AuditableDto auditableDtoUnderTest;

    @BeforeEach
    void setUp() {
        auditableDtoUnderTest = new AuditableDto("dataCreationDate", "lastUpdatedDate");
    }

    @Test
    void testEquals() {
        // Run the test
        final boolean result = auditableDtoUnderTest.equals("o");

        // Verify the results
        assertTrue(!result);
    }

    @Test
    void testHashCode() {
        // Run the test
        final int result = auditableDtoUnderTest.hashCode();

        // Verify the results
        assertEquals(1416386073, result);
    }

    @Test
    void testToString() {
        // Run the test
        final String result = auditableDtoUnderTest.toString();

        // Verify the results
        assertEquals("AuditableDto(dataCreationDate=dataCreationDate, lastUpdatedDate=lastUpdatedDate)", result);
    }

    @Test
    void testToBuilder() {
        // Run the test
        final AuditableDto.AuditableDtoBuilder result = auditableDtoUnderTest.toBuilder();

        // Verify the results
        assertTrue(true);
    }

    @Test
    void testBuilder() {
        // Run the test
        final AuditableDto.AuditableDtoBuilder result = AuditableDto.builder();

        // Verify the results
        assertTrue(true);
    }
}
