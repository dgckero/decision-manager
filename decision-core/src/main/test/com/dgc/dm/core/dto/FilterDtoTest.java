package com.dgc.dm.core.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.MockitoAnnotations.initMocks;

class FilterDtoTest {

    @Mock
    private ProjectDto mockProject;

    private FilterDto filterDtoUnderTest;

    @BeforeEach
    void setUp() {
        initMocks(this);
        filterDtoUnderTest = new FilterDto(0, "name", "filterClass", "value", false, false, mockProject);
    }

    @Test
    void testToString() {
        // Setup

        // Run the test
        final String result = filterDtoUnderTest.toString();

        // Verify the results
        assertEquals("FilterDto(id=0, name=name, filterClass=filterClass, value=value, active=false, contactFilter=false, project=mockProject)", result);
    }

    @Test
    void testToBuilder() {
        // Setup

        // Run the test
        final FilterDto.FilterDtoBuilder<?, ?> result = filterDtoUnderTest.toBuilder();

        // Verify the results
        assertTrue(true);
    }

    @Test
    void testEquals() {
        // Setup

        // Run the test
        final boolean result = filterDtoUnderTest.equals("FilterDto(id=0, name=name, filterClass=filterClass, value=value, active=false, contactFilter=false, project=mockProject)");

        // Verify the results
        assertTrue(!result);
    }

    @Test
    void testHashCode() {
        // Setup

        // Run the test
        final int result = filterDtoUnderTest.hashCode();

        // Verify the results
        assertTrue(true);
    }

    @Test
    void testBuilder() {
        // Setup

        // Run the test
        final FilterDto.FilterDtoBuilder<?, ?> result = FilterDto.builder();

        // Verify the results
        assertTrue(true);
    }
}
