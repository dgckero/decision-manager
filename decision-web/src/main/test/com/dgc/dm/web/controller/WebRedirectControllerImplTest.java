package com.dgc.dm.web.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WebRedirectControllerImplTest {

    private WebRedirectControllerImpl webRedirectControllerImplUnderTest;

    @BeforeEach
    void setUp() {
        webRedirectControllerImplUnderTest = new WebRedirectControllerImpl();
    }

    @Test
    void testHome() {
        // Run the test
        final ModelAndView result = webRedirectControllerImplUnderTest.home();

        // Verify the results
        assertEquals(result.getViewName(), CommonController.HOME_VIEW);
    }

    @Test
    void testNewProject() {
        // Run the test
        final ModelAndView result = webRedirectControllerImplUnderTest.newProject();

        // Verify the results
        assertEquals(result.getViewName(), CommonController.NEW_PROJECT_VIEW);
    }

    @Test
    void testInformation() {
        // Run the test
        final ModelAndView result = webRedirectControllerImplUnderTest.information();

        // Verify the results
        assertEquals(result.getViewName(), CommonController.INFORMATION_VIEW);
    }
}
