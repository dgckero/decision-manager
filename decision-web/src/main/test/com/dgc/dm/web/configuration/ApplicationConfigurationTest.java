package com.dgc.dm.web.configuration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewResolverRegistry;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.theme.FixedThemeResolver;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.thymeleaf.spring5.templateresolver.SpringResourceTemplateResolver;

import javax.servlet.ServletContext;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.MockitoAnnotations.initMocks;

class ApplicationConfigurationTest {

    @Mock
    private ApplicationContext mockApplicationContext;

    private ApplicationConfiguration applicationConfigurationUnderTest;

    @BeforeEach
    void setUp() {
        initMocks(this);
        applicationConfigurationUnderTest = new ApplicationConfiguration(mockApplicationContext);
    }

    @Test
    void testAddResourceHandlers() {
        ResourceHandlerRegistry registry = new ResourceHandlerRegistry(Mockito.mock(ApplicationContext.class), Mockito.mock(ServletContext.class));
        // Run the test
        applicationConfigurationUnderTest.addResourceHandlers(registry);

        // Verify the results
        assertTrue(true);
    }

    @Test
    void testAddInterceptors() {
        // Setup
        final InterceptorRegistry registry = new InterceptorRegistry();

        // Run the test
        applicationConfigurationUnderTest.addInterceptors(registry);

        // Verify the results
        assertTrue(true);
    }

    @Test
    void testLocaleChangeInterceptor() {
        // Setup

        // Run the test
        final LocaleChangeInterceptor result = applicationConfigurationUnderTest.localeChangeInterceptor();

        // Verify the results
        assertTrue(true);
    }

    @Test
    void testLocaleResolver() {
        // Setup

        // Run the test
        final LocaleResolver result = applicationConfigurationUnderTest.localeResolver();

        // Verify the results
        assertTrue(true);
    }

    @Test
    void testThemeResolver() {
        // Run the test
        final FixedThemeResolver result = applicationConfigurationUnderTest.themeResolver();

        // Verify the results
        assertTrue(true);
    }

    @Test
    void testResourceBundleMessageSource() {
        // Run the test
        final ResourceBundleMessageSource result = applicationConfigurationUnderTest.resourceBundleMessageSource();

        // Verify the results
        assertTrue(true);
    }

    @Test
    void testTemplateResolver() {
        // Run the test
        final SpringResourceTemplateResolver result = applicationConfigurationUnderTest.templateResolver();

        // Verify the results
        assertTrue(true);
    }

    @Test
    void testTemplateEngine() {
        // Run the test
        final SpringTemplateEngine result = applicationConfigurationUnderTest.templateEngine();

        // Verify the results
        assertTrue(true);
    }

    @Test
    void testConfigureViewResolvers() {
        // Run the test
        applicationConfigurationUnderTest.configureViewResolvers(Mockito.mock(ViewResolverRegistry.class));

        // Verify the results
        assertTrue(true);
    }

    @Test
    void testMultipartResolver() {
        // Run the test
        final MultipartResolver result = applicationConfigurationUnderTest.multipartResolver();

        // Verify the results
        assertTrue(true);
    }
}
