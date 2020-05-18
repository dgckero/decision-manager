package com.dgc.dm.core.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProjectDtoTest {

    private ProjectDto projectDtoUnderTest;

    @BeforeEach
    void setUp() {
        projectDtoUnderTest = new ProjectDto(0, "name", "rowDataTableName", "emailTemplate", "content".getBytes());
    }

    @Test
    void testToString() {
        // Setup

        // Run the test
        final String result = projectDtoUnderTest.toString();

        // Verify the results
        assertEquals("ProjectDto(id=0, name=name, rowDataTableName=rowDataTableName, emailTemplate=emailTemplate)", result);
    }

    @Test
    void testToBuilder() {
        // Setup

        // Run the test
        final ProjectDto.ProjectDtoBuilder<?, ?> result = projectDtoUnderTest.toBuilder();

        // Verify the results
        assertTrue(true);
    }

    @Test
    void testEquals() {
        // Setup

        // Run the test
        final boolean result = projectDtoUnderTest.equals("o");

        // Verify the results
        assertTrue(!result);
    }

    @Test
    void testHashCode() {
        // Setup

        // Run the test
        final int result = projectDtoUnderTest.hashCode();

        // Verify the results
        assertEquals(293882695, result);
    }

    @Test
    void testBuilder() {
        // Setup

        // Run the test
        final ProjectDto.ProjectDtoBuilder<?, ?> result = ProjectDto.builder();

        // Verify the results
        assertTrue(true);
    }
}
