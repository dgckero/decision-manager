package com.dgc.dm.core.service.bpmn;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DecisionManagerDateDataTypeTransformerTest {

    @InjectMocks
    private DecisionManagerDateDataTypeTransformer decisionManagerDateDataTypeTransformerUnderTest;

    @BeforeEach
    void setUp() {
        decisionManagerDateDataTypeTransformerUnderTest = new DecisionManagerDateDataTypeTransformer();
    }

    @Test
    void transformString() {

        decisionManagerDateDataTypeTransformerUnderTest.transformString("2020-05-18");
        assertTrue(true);
    }

    @Test
    void transformString_throwsIllegalArgumentException() {

        assertThrows(IllegalArgumentException.class, () -> {
            decisionManagerDateDataTypeTransformerUnderTest.transformString("no format date");
        });
    }
}
