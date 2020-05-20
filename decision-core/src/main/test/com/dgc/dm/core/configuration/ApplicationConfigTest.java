package com.dgc.dm.core.configuration;

import org.hibernate.internal.SessionFactoryImpl;
import org.junit.Rule;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.core.env.Environment;
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

class ApplicationConfigTest {

    @Rule
    final EnvironmentVariables environmentVariables = new EnvironmentVariables();

    @Mock
    private Environment mockEnv;

    @InjectMocks
    private ApplicationConfig applicationConfigUnderTest;

    private static final String HIBERNATE_DIALECT = "hibernate.dialect";
    private static final String HIBERNATE_SHOW_SQL = "hibernate.show_sql";
    private static final String CORE_PACKAGE = "com.dgc.dm.core.db";
    private static final String MAIL_TRANSPORT_PROTOCOL = "mail.transport.protocol";
    private static final String MAIL_DEBUG = "mail.debug";
    private static final String JASYPT_ENVIRONMENT_ALGORITHM = "jasypt.environment.algorithm";
    private static final String JASYPT_ENVIRONMENT_NAME = "jasypt.environment.name";
    private static final String MAIL_HOST = "mail.host";
    private static final String MAIL_PORT = "mail.port";
    private static final String MAIL_USERNAME = "mail.username";
    private static final String MAIL_PASSWORD = "mail.password";
    private static final String MAIL_SMTP_AUTH = "mail.smtp.auth";
    private static final String MAIL_SMTP_STARTTLS_ENABLE = "mail.smtp.starttls.enable";
    private static final String JDBC_DRIVER_CLASS_NAME = "jdbc.driverClassName";
    private static final String JDBC_URL = "jdbc.url";
    private static final String JDBC_USER = "jdbc.user";
    private static final String JDBC_PASS = "jdbc.pass";

    @BeforeEach
    void setUp() {
        initMocks(this);
    }

    @Test
    void testModelMapper() {
        // Run the test
        final ModelMapper result = applicationConfigUnderTest.modelMapper();

        // Verify the results
        assertEquals(MatchingStrategies.STRICT, result.getConfiguration().getMatchingStrategy());
    }

    @Test
    void testDataSource() {
        // Setup
        when(mockEnv.getProperty(JDBC_DRIVER_CLASS_NAME)).thenReturn("org.sqlite.JDBC");
        when(mockEnv.getProperty(JDBC_URL)).thenReturn("jdbc:sqlite:decisiondb.sqlite");
        when(mockEnv.getProperty(JDBC_DRIVER_CLASS_NAME)).thenReturn("org.sqlite.JDBC");
        when(mockEnv.getProperty(JDBC_USER)).thenReturn("user");
        when(mockEnv.getProperty(JDBC_PASS)).thenReturn("password");

        // Run the test
        final DataSource result = applicationConfigUnderTest.dataSource();

        // Verify the results
        assertNotNull(result);
    }

    @Test
    void testSessionFactory() {
        // Setup
        when(mockEnv.getProperty(JDBC_DRIVER_CLASS_NAME)).thenReturn("org.sqlite.JDBC");
        when(mockEnv.getProperty(JDBC_URL)).thenReturn("jdbc:sqlite:decisiondb.sqlite");
        when(mockEnv.getProperty(JDBC_DRIVER_CLASS_NAME)).thenReturn("org.sqlite.JDBC");
        when(mockEnv.getProperty(JDBC_USER)).thenReturn("user");
        when(mockEnv.getProperty(JDBC_PASS)).thenReturn("password");

        when(mockEnv.getProperty(HIBERNATE_DIALECT)).thenReturn("org.hibernate.dialect.SQLiteDialect");
        when(mockEnv.getProperty(HIBERNATE_SHOW_SQL)).thenReturn("true");

        // Run the test
        final LocalSessionFactoryBean result = applicationConfigUnderTest.sessionFactory();

        // Verify the results
        assertNotNull(result);
    }

    @Test
    void testTransactionManager() {
        // Setup
        when(mockEnv.getProperty(JDBC_DRIVER_CLASS_NAME)).thenReturn("org.sqlite.JDBC");
        when(mockEnv.getProperty(JDBC_URL)).thenReturn("jdbc:sqlite:decisiondb.sqlite");
        when(mockEnv.getProperty(JDBC_DRIVER_CLASS_NAME)).thenReturn("org.sqlite.JDBC");
        when(mockEnv.getProperty(JDBC_USER)).thenReturn("user");
        when(mockEnv.getProperty(JDBC_PASS)).thenReturn("password");

        EntityManagerFactory mockEntityManagerFactory = mock(SessionFactoryImpl.class);

        // Run the test
        final PlatformTransactionManager result = applicationConfigUnderTest.transactionManager(mockEntityManagerFactory);

        // Verify the results
        assertNotNull(result);

    }

    @Test
    void testAnnotationDrivenTransactionManager() {
        // Setup
        when(mockEnv.getProperty(JDBC_DRIVER_CLASS_NAME)).thenReturn("org.sqlite.JDBC");
        when(mockEnv.getProperty(JDBC_URL)).thenReturn("jdbc:sqlite:decisiondb.sqlite");
        when(mockEnv.getProperty(JDBC_DRIVER_CLASS_NAME)).thenReturn("org.sqlite.JDBC");
        when(mockEnv.getProperty(JDBC_USER)).thenReturn("user");
        when(mockEnv.getProperty(JDBC_PASS)).thenReturn("password");

        when(mockEnv.getProperty(HIBERNATE_DIALECT)).thenReturn("org.hibernate.dialect.SQLiteDialect");
        when(mockEnv.getProperty(HIBERNATE_SHOW_SQL)).thenReturn("true");

        // Run the test
        final PlatformTransactionManager result = applicationConfigUnderTest.annotationDrivenTransactionManager();

        // Verify the results
        assertNotNull(result);

    }

    @Test
    void testJdbcTemplate() {
        // Setup
        when(mockEnv.getProperty(JDBC_DRIVER_CLASS_NAME)).thenReturn("org.sqlite.JDBC");
        when(mockEnv.getProperty(JDBC_URL)).thenReturn("jdbc:sqlite:decisiondb.sqlite");
        when(mockEnv.getProperty(JDBC_DRIVER_CLASS_NAME)).thenReturn("org.sqlite.JDBC");
        when(mockEnv.getProperty(JDBC_USER)).thenReturn("user");
        when(mockEnv.getProperty(JDBC_PASS)).thenReturn("password");

        // Run the test
        final JdbcTemplate result = applicationConfigUnderTest.jdbcTemplate();

        // Verify the results
        assertNotNull(result);
    }

    @Test
    void testGetJavaMailSender() {
        // Setup
        when(mockEnv.getProperty(MAIL_HOST)).thenReturn("smtp.gmail.com");
        when(mockEnv.getProperty(MAIL_PORT)).thenReturn("25");
        when(mockEnv.getProperty(MAIL_USERNAME)).thenReturn("ENC(test)");
        when(mockEnv.getProperty(MAIL_PASSWORD)).thenReturn("TEST");

        when(mockEnv.getProperty(MAIL_TRANSPORT_PROTOCOL)).thenReturn("smtp");
        when(mockEnv.getProperty(MAIL_SMTP_AUTH)).thenReturn("true");
        when(mockEnv.getProperty(MAIL_SMTP_STARTTLS_ENABLE)).thenReturn("true");
        when(mockEnv.getProperty(MAIL_DEBUG)).thenReturn("true");

        // Run the test
        final JavaMailSender result = applicationConfigUnderTest.getJavaMailSender();

        // Verify the results
        assertNotNull(result);
    }

    @Test
    void testGetPropertyValue() {
        // Setup
        when(mockEnv.getProperty(JASYPT_ENVIRONMENT_ALGORITHM)).thenReturn("PBEWithMD5AndDES");
        when(mockEnv.getProperty(JASYPT_ENVIRONMENT_NAME)).thenReturn("DECISION_ENC_PASSWORD");
        environmentVariables.set("DECISION_ENC_PASSWORD", "test");
        String encryptedProperty = "ENC(test=)";

        // Run the test
        final String result = applicationConfigUnderTest.getPropertyValue(encryptedProperty);

        // Verify the results
        assertNull(result);
    }

    @Test
    void testExceptionTranslation() {
        // Setup

        // Run the test
        final PersistenceExceptionTranslationPostProcessor result = applicationConfigUnderTest.exceptionTranslation();

        // Verify the results
        assertNotNull(result);
    }

    @Test
    void testGetHibernateProperties() {
        // Setup
        final Properties expectedResult = new Properties();
        when(mockEnv.getProperty(any())).thenReturn("result");

        // Run the test
        final Properties result = applicationConfigUnderTest.getHibernateProperties();

        // Verify the results
        assertNotNull(result);
    }
}
