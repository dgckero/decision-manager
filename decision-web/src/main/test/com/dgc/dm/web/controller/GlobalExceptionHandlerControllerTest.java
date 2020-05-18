package com.dgc.dm.web.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.NoHandlerFoundException;

import static com.dgc.dm.web.controller.CommonController.MODEL_MESSAGE;
import static com.dgc.dm.web.controller.GlobalExceptionHandlerController.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class GlobalExceptionHandlerControllerTest {

    private GlobalExceptionHandlerController globalExceptionHandlerControllerUnderTest;

    @BeforeEach
    void setUp() {
        globalExceptionHandlerControllerUnderTest = new GlobalExceptionHandlerController();
    }

    @Test
    void testHandleNullPointerException() {
        // Setup
        final Exception e = new Exception("s");

        // Run the test
        final ModelAndView result = globalExceptionHandlerControllerUnderTest.handleNullPointerException(e);

        // Verify the results
        assertEquals(GENERIC_SERVER_ERROR, result.getModel().get(MODEL_MESSAGE));
    }

    @Test
    void testHandleDecisionException() {
        // Setup
        final Exception e = new Exception("test");

        // Run the test
        final ModelAndView result = globalExceptionHandlerControllerUnderTest.handleDecisionException(e);

        // Verify the results
        assertEquals("test", result.getModel().get(MODEL_MESSAGE));
    }

    @Test
    void testHandleAllException() {
        // Setup
        final Exception e = new Exception("s");

        // Run the test
        final ModelAndView result = globalExceptionHandlerControllerUnderTest.handleAllException(e);

        // Verify the results
        assertEquals(GENERIC_SERVER_ERROR, result.getModel().get(MODEL_MESSAGE));
    }

    @Test
    void testHandleResourceNotFoundException() {
        // Run the test
        final ModelAndView result = globalExceptionHandlerControllerUnderTest.handleResourceNotFoundException();

        // Verify the results
        assertEquals(RESOURCE_NOT_FOUND, result.getModel().get(MODEL_MESSAGE));
    }

    @Test
    void testHandle() {
        // Setup
        final NoHandlerFoundException ex = new NoHandlerFoundException("httpMethod", "requestURL", null);

        // Run the test
        final ModelAndView result = globalExceptionHandlerControllerUnderTest.handle(ex);

        // Verify the results
        assertEquals(PAGE_NOT_FOUND, result.getModel().get(MODEL_MESSAGE));
    }
}
