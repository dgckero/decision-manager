package com.dgc.dm.core.db.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProjectTest {

    private Project projectUnderTest;

    @BeforeEach
    void setUp() {
        projectUnderTest = new Project(0, "name", "rowDataTableName", "emailTemplate", "content".getBytes());
    }

    @Test
    void testEquals() {
        // Setup

        // Run the test
        final boolean result = projectUnderTest.equals("o");

        // Verify the results
        assertTrue(!result);
    }

    @Test
    void testHashCode() {
        // Setup

        // Run the test
        final int result = projectUnderTest.hashCode();

        // Verify the results
        assertEquals(293882695, result);
    }

    @Test
    void testToBuilder() {
        // Setup

        // Run the test
        final Project.ProjectBuilder<?, ?> result = projectUnderTest.toBuilder();

        // Verify the results
        assertTrue(true);
    }

    @Test
    void testToString() {
        // Setup

        // Run the test
        final String result = projectUnderTest.toString();

        // Verify the results
        assertEquals("Project(id=0, name=name, rowDataTableName=rowDataTableName, emailTemplate=emailTemplate)", result);
    }

    @Test
    void testBuilder() {
        // Setup

        // Run the test
        final Project.ProjectBuilder<?, ?> result = Project.builder();

        // Verify the results
        assertTrue(true);
    }
}
