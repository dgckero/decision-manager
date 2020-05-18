package com.dgc.dm.core.db.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.MockitoAnnotations.initMocks;

class FilterTest {

    @Mock
    private Project mockProject;

    private Filter filterUnderTest;

    @BeforeEach
    void setUp() {
        initMocks(this);
        filterUnderTest = new Filter(0, "name", "filterClass", "value", false, false, mockProject);
    }

    @Test
    void testToString() {
        // Setup

        // Run the test
        final String result = filterUnderTest.toString();

        // Verify the results
        assertEquals("Filter(id=0, name=name, filterClass=filterClass, value=value, active=false, contactFilter=false, project=mockProject)", result);
    }

    @Test
    void testToBuilder() {
        // Setup

        // Run the test
        final Filter.FilterBuilder<?, ?> result = filterUnderTest.toBuilder();

        // Verify the results
        assertTrue(true);
    }

    @Test
    void testEquals() {
        // Setup

        // Run the test
        final boolean result = filterUnderTest.equals("o");

        // Verify the results
        assertTrue(!result);
    }

    @Test
    void testHashCode() {
        // Setup

        // Run the test
        final int result = filterUnderTest.hashCode();

        // Verify the results
        assertTrue(true);
    }

    @Test
    void testBuilder() {
        // Setup

        // Run the test
        final Filter.FilterBuilder<?, ?> result = Filter.builder();

        // Verify the results
        assertTrue(true);
    }
}
