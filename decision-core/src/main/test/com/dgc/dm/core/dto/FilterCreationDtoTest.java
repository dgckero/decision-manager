package com.dgc.dm.core.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FilterCreationDtoTest {

    private FilterCreationDto filterCreationDtoUnderTest;

    @BeforeEach
    void setUp() {
        filterCreationDtoUnderTest = new FilterCreationDto(Arrays.asList(new FilterDto(0, "name", "filterClass", "value", false, false, new ProjectDto(0, "name", "rowDataTableName", "emailTemplate", "content".getBytes()))));
    }

    @Test
    void testEquals() {
        // Setup

        // Run the test
        final boolean result = filterCreationDtoUnderTest.equals("o");

        // Verify the results
        assertTrue(!result);
    }

    @Test
    void testHashCode() {
        // Setup

        // Run the test
        final int result = filterCreationDtoUnderTest.hashCode();

        // Verify the results
        assertEquals(-173389880, result);
    }

    @Test
    void testToString() {
        // Setup

        // Run the test
        final String result = filterCreationDtoUnderTest.toString();

        // Verify the results
        assertEquals("FilterCreationDto(filters=[FilterDto(id=0, name=name, filterClass=filterClass, value=value, active=false, contactFilter=false, project=ProjectDto(id=0, name=name, rowDataTableName=rowDataTableName, emailTemplate=emailTemplate))])", result);
    }
}
