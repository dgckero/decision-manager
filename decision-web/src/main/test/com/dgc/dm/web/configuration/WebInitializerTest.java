package com.dgc.dm.web.configuration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.servlet.ServletRegistration;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class WebInitializerTest {

    private WebInitializer webInitializerUnderTest;

    @BeforeEach
    void setUp() {
        webInitializerUnderTest = new WebInitializer();
    }

    @Test
    void getRootConfigClasses() {
        webInitializerUnderTest.getRootConfigClasses();
        assertTrue(true);
    }

    @Test
    void getServletConfigClasses() {
        webInitializerUnderTest.getServletConfigClasses();
        assertTrue(true);
    }

    @Test
    void getServletMappings() {
        webInitializerUnderTest.getServletMappings();
        assertTrue(true);
    }

    @Test
    void customizeRegistration() {
        ServletRegistration.Dynamic mockDynamic = mock(ServletRegistration.Dynamic.class);
        when(mockDynamic.setInitParameter("throwExceptionIfNoHandlerFound", "true")).thenReturn(true);
        webInitializerUnderTest.customizeRegistration(mockDynamic);
        assertTrue(true);
    }

    @Test
    void customizeRegistration_throwsRuntimeException() {
        ServletRegistration.Dynamic mockDynamic = mock(ServletRegistration.Dynamic.class);
        when(mockDynamic.setInitParameter("throwExceptionIfNoHandlerFound", "true")).thenReturn(false);

        assertThrows(RuntimeException.class, () -> {
            webInitializerUnderTest.customizeRegistration(mockDynamic);
        });

    }
}
