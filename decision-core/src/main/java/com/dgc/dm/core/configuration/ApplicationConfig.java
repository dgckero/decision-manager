/*
  @author david
 */

package com.dgc.dm.core.configuration;

import com.google.common.base.Preconditions;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.EnvironmentStringPBEConfig;
import org.jasypt.encryption.pbe.config.PBEConfig;
import org.jasypt.properties.PropertyValueEncryptionUtils;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import org.springframework.core.env.Environment;
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.TransactionManagementConfigurer;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.Properties;

@NoArgsConstructor
@Log4j2
@Configuration
@PropertySource({
        "classpath:application.properties",
        "classpath:persistence.properties",
        "classpath:smtp.properties"
})
@ComponentScan("com.dgc.dm.core")
@EnableJpaRepositories(
        basePackages = "com.dgc.dm.core.db",
        entityManagerFactoryRef = "sessionFactory")
@EnableTransactionManagement
@EnableJpaAuditing
public class ApplicationConfig implements TransactionManagementConfigurer {

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

    private Environment env;

    @Autowired
    public ApplicationConfig(Environment env) {
        this.env = env;
    }

    /**
     * Set configuration for modelMapper
     * modelMapper is user to convert from DTO to Entity or vice-versa
     *
     * @return modelMapper
     */
    @Bean
    public ModelMapper modelMapper() {
        log.debug("[INIT] Configuring modelMapper");

        final ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration()
                .setMatchingStrategy(MatchingStrategies.STRICT);

        log.debug("[END] modelMapper successfully configured");
        return modelMapper;
    }

    /**
     * Add hibernate properties
     *
     * @return hibernateProperties
     */
    final Properties getHibernateProperties() {
        log.debug("[INIT] Configuring additionalProperties");

        Properties hibernateProperties = new Properties();
        hibernateProperties.setProperty(HIBERNATE_DIALECT, getPropertyValue(HIBERNATE_DIALECT));
        hibernateProperties.setProperty(HIBERNATE_SHOW_SQL, getPropertyValue(HIBERNATE_SHOW_SQL));

        log.debug("[END] Configuring additionalProperties");
        return hibernateProperties;
    }

    /**
     * Add data base information to dataSource
     *
     * @return dataSource
     */
    @Bean
    public DataSource dataSource() {
        log.debug("[INIT] Configuring dataSource");

        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(Preconditions.checkNotNull(getPropertyValue(JDBC_DRIVER_CLASS_NAME)));
        dataSource.setUrl(Preconditions.checkNotNull(getPropertyValue(JDBC_URL)));
        dataSource.setUsername(Preconditions.checkNotNull(getPropertyValue(JDBC_USER)));
        dataSource.setPassword(Preconditions.checkNotNull(getPropertyValue(JDBC_PASS)));

        log.debug("[END] Configuring dataSource");
        return dataSource;
    }

    /**
     * Return factory associated with the persistence context
     *
     * @return entityManagerFactory
     */
    @Bean
    public LocalSessionFactoryBean sessionFactory() {
        log.debug("[INIT] Configuring sessionFactory");

        final LocalSessionFactoryBean sessionFactory = new LocalSessionFactoryBean();
        sessionFactory.setDataSource(this.dataSource());
        sessionFactory.setPackagesToScan(CORE_PACKAGE);
        sessionFactory.setHibernateProperties(this.getHibernateProperties());

        log.debug("[END] Configuring sessionFactory");
        return sessionFactory;
    }

    /**
     * Set the EntityManagerFactory that this instance should manage transactions for
     *
     * @param entityManagerFactory
     * @return JpaTransactionManager
     */
    @Bean(name = "transactionManager")
    public PlatformTransactionManager transactionManager(final EntityManagerFactory entityManagerFactory) {
        log.debug("[INIT] Configuring transactionManager for entityManagerFactory: " + entityManagerFactory);

        final DataSourceTransactionManager txManager = new DataSourceTransactionManager();
        txManager.setDataSource(this.dataSource());

        log.debug("[END] Configuring transactionManager");
        return txManager;
    }

    /**
     * Return the default transaction manager bean to use for annotation-driven database
     * transaction management, i.e. when processing {@code @Transactional} methods.
     */
    @Override
    @Bean
    @DependsOn("sessionFactory")
    public PlatformTransactionManager annotationDrivenTransactionManager() {
        log.debug("[INIT] Configuring annotationDrivenTransactionManager");

        final JpaTransactionManager jpa = new JpaTransactionManager();
        jpa.setEntityManagerFactory(this.sessionFactory().getObject());

        log.debug("[END] Configuring annotationDrivenTransactionManager");
        return jpa;
    }

    /**
     * Applies persistence exception translation to any
     * bean marked with Spring's @{@link org.springframework.stereotype.Repository Repository}
     * annotation
     *
     * @return PersistenceExceptionTranslationPostProcessor
     */
    @Bean
    public PersistenceExceptionTranslationPostProcessor exceptionTranslation() {
        log.debug("[INIT] Configuring exceptionTranslation");
        final PersistenceExceptionTranslationPostProcessor persistenceExceptionTranslationPostProcessor = new PersistenceExceptionTranslationPostProcessor();
        log.debug("[END] Configuring exceptionTranslation");
        return persistenceExceptionTranslationPostProcessor;
    }

    /**
     * Return a new JdbcTemplate, given a DataSource to obtain connections from.
     *
     * @return JdbcTemplate
     */
    @Bean
    public JdbcTemplate jdbcTemplate() {
        log.debug("[INIT] Configuring jdbcTemplate");
        final JdbcTemplate jdbcTemplate = new JdbcTemplate(this.dataSource());
        log.debug("[END] Configuring jdbcTemplate");
        return jdbcTemplate;
    }

    /**
     * Set configuration for mail sender
     *
     * @return JavaMailSender
     */
    @Bean
    public JavaMailSender getJavaMailSender() {
        log.debug("[INIT] Configuring Mail Sender");

        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(getPropertyValue(MAIL_HOST));
        mailSender.setPort(Integer.parseInt(getPropertyValue(MAIL_PORT)));
        mailSender.setUsername(getPropertyValue(MAIL_USERNAME));
        mailSender.setPassword(getPropertyValue(MAIL_PASSWORD));

        Properties props = mailSender.getJavaMailProperties();
        props.put(MAIL_TRANSPORT_PROTOCOL, getPropertyValue(MAIL_TRANSPORT_PROTOCOL));
        props.put(MAIL_SMTP_AUTH, getPropertyValue(MAIL_SMTP_AUTH));
        props.put(MAIL_SMTP_STARTTLS_ENABLE, getPropertyValue(MAIL_SMTP_STARTTLS_ENABLE));
        props.put(MAIL_DEBUG, getPropertyValue(MAIL_DEBUG));

        log.debug("[END] Mail Sender successfully configured ");
        return mailSender;
    }

    /**
     * Get property value
     *
     * @param property
     * @return property value
     */
    public String getPropertyValue(String property) {
        log.debug("[INIT] getPropertyValue: {}", property);
        String propertyVal = env.getProperty(property);
        if (PropertyValueEncryptionUtils.isEncryptedValue(propertyVal)) {
            log.trace("property: {} is encrypted", property);
            propertyVal = getEncryptedProperty(propertyVal);
        }
        log.debug("[END] getPropertyValue: {}", property);
        return propertyVal;
    }

    /**
     * Decrypt encryptedPropery
     *
     * @param encryptedProperty
     * @return decrypted propery
     */
    private String getEncryptedProperty(String encryptedProperty) {
        log.debug("[INIT] decrypting property");
        String decryptedProperty = null;
        try {
            decryptedProperty = PropertyValueEncryptionUtils.decrypt(encryptedProperty, getEncryptor());
        } catch (NullPointerException e) {
            log.error("JASYPT ENVIRONMENT VARIABLE HAS NOT BEEN DEFINED");
        }
        log.debug("[END] decrypting property ");
        return decryptedProperty;
    }

    /**
     * Get Encryptor
     *
     * @return encryptor
     */
    private StandardPBEStringEncryptor getEncryptor() {
        log.debug("[INIT] getEncryptor");
        StandardPBEStringEncryptor standardPBEStringEncryptor = new StandardPBEStringEncryptor();
        standardPBEStringEncryptor.setConfig(getEnvironmentConfig());
        log.debug("[END] getEncryptor");

        return standardPBEStringEncryptor;
    }

    /**
     * Get PBEConfig based on jasypt properties
     *
     * @return EnvironmentStringPBEConfig
     */
    private PBEConfig getEnvironmentConfig() {
        log.debug("[INIT] getEnvironmentConfig");
        EnvironmentStringPBEConfig config = new EnvironmentStringPBEConfig();
        config.setAlgorithm(env.getProperty(JASYPT_ENVIRONMENT_ALGORITHM));
        config.setPasswordEnvName(env.getProperty(JASYPT_ENVIRONMENT_NAME));
        log.debug("[END] getEnvironmentConfig");
        return config;
    }

}
