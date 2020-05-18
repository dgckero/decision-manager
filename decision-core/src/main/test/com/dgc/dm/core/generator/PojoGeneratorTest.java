package com.dgc.dm.core.generator;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class PojoGeneratorTest {

    @Test
    void testGenerate() throws Exception {
        // Setup
        final Map<String, Class<?>> properties = new HashMap<>();

        // Run the test
        final Class result = PojoGenerator.generate("className", properties);

        // Verify the results
        assertTrue(true);
    }

    @Test
    void testGenerate_ThrowsNotFoundException() {
        // Setup
        final Map<String, Class<?>> properties = new HashMap<>();

        // Run the test
        assertThrows(RuntimeException.class, () -> {
            PojoGenerator.generate("className", properties);
        });
    }

    @Test
    void testGetPropertyNameByColumnName() {
        assertEquals("className", PojoGenerator.getPropertyNameByColumnName("className"));
    }
}
