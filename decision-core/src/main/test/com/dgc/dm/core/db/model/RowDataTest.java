package com.dgc.dm.core.db.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.MockitoAnnotations.initMocks;

class RowDataTest {

    @Mock
    private Project mockProject;

    private RowData rowDataUnderTest;

    @BeforeEach
    void setUp() {
        initMocks(this);
        rowDataUnderTest = new RowData(0, mockProject);
    }

    @Test
    void testToBuilder() {
        // Setup

        // Run the test
        final RowData.RowDataBuilder<?, ?> result = rowDataUnderTest.toBuilder();

        // Verify the results
        assertTrue(true);
    }

    @Test
    void testToString() {
        // Setup

        // Run the test
        final String result = rowDataUnderTest.toString();

        // Verify the results
        assertEquals("RowData(rowId=0, project=mockProject)", result);
    }

    @Test
    void testEquals() {
        // Setup

        // Run the test
        final boolean result = rowDataUnderTest.equals("o");

        // Verify the results
        assertTrue(!result);
    }

    @Test
    void testHashCode() {
        // Setup

        // Run the test
        final int result = rowDataUnderTest.hashCode();

        // Verify the results
        assertTrue(true);
    }

    @Test
    void testBuilder() {
        // Setup

        // Run the test
        final RowData.RowDataBuilder<?, ?> result = RowData.builder();

        // Verify the results
        assertTrue(true);
    }
}
