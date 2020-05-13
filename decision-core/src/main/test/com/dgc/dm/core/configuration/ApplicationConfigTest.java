package com.dgc.dm.core.configuration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.modelmapper.ModelMapper;
import org.springframework.core.env.Environment;
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

class ApplicationConfigTest {

    @Mock
    private Environment mockEnv;

    @InjectMocks
    private ApplicationConfig applicationConfigUnderTest;

    @BeforeEach
    void setUp() {
        initMocks(this);
    }

    @Test
    void testModelMapper() {
        // Setup

        // Run the test
        final ModelMapper result = applicationConfigUnderTest.modelMapper();

        // Verify the results
    }

    @Test
    void testExceptionTranslation() {
        // Setup

        // Run the test
        final PersistenceExceptionTranslationPostProcessor result = applicationConfigUnderTest.exceptionTranslation();

        // Verify the results
        assertTrue(true);
    }

    @Test
    void testGetHibernateProperties() {
        // Setup
        final Properties expectedResult = new Properties();
        when(mockEnv.getProperty(any())).thenReturn("result");

        // Run the test
        final Properties result = applicationConfigUnderTest.getHibernateProperties();

        // Verify the results
        assertTrue(true);
    }
}
