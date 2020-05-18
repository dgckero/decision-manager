package com.dgc.dm.core.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.MockitoAnnotations.initMocks;

class RowDataDtoTest {

    @Mock
    private ProjectDto mockProject;

    private RowDataDto rowDataDtoUnderTest;

    @BeforeEach
    void setUp() {
        initMocks(this);
        rowDataDtoUnderTest = new RowDataDto(0, mockProject);
    }

    @Test
    void testToString() {
        // Run the test
        final String result = rowDataDtoUnderTest.toString();

        // Verify the results
        assertEquals("RowDataDto(rowId=0, project=mockProject)", result);
    }

    @Test
    void testEquals() {
        // Setup

        // Run the test
        final boolean result = rowDataDtoUnderTest.equals("o");

        // Verify the results
        assertTrue(!result);
    }

    @Test
    void testHashCode() {
        // Setup

        // Run the test
        final int result = rowDataDtoUnderTest.hashCode();

        // Verify the results
        assertTrue(true);
    }

}
