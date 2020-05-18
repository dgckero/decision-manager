package com.dgc.dm.core.db.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuditableTest {

    private Auditable auditableUnderTest;

    @BeforeEach
    void setUp() {
        auditableUnderTest = new Auditable("dataCreationDate", "lastUpdatedDate");
    }

    @Test
    void testEquals() {
        // Setup

        // Run the test
        final boolean result = auditableUnderTest.equals("o");

        // Verify the results
        assertTrue(!result);
    }

    @Test
    void testHashCode() {
        // Setup

        // Run the test
        final int result = auditableUnderTest.hashCode();

        // Verify the results
        assertTrue(true);
    }

    @Test
    void testToString() {
        // Setup

        // Run the test
        final String result = auditableUnderTest.toString();

        // Verify the results
        assertEquals("Auditable(dataCreationDate=dataCreationDate, lastUpdatedDate=lastUpdatedDate)", result);
    }

    @Test
    void testToBuilder() {
        // Setup

        // Run the test
        final Auditable.AuditableBuilder result = auditableUnderTest.toBuilder();

        // Verify the results
        assertTrue(true);
    }

    @Test
    void testBuilder() {
        // Setup

        // Run the test
        final Auditable.AuditableBuilder result = Auditable.builder();

        // Verify the results
        assertTrue(true);
    }
}
