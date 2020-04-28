/*
  @author david
 */

package com.dgc.dm.core.configuration;

import com.google.common.base.Preconditions;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
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
public class ApplicationConfig implements TransactionManagementConfigurer {

    @Autowired
    private Environment env;

    public ApplicationConfig() {
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
        hibernateProperties.setProperty("hibernate.dialect", this.env.getProperty("hibernate.dialect"));
        hibernateProperties.setProperty("hibernate.show_sql", this.env.getProperty("hibernate.show_sql"));

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
        dataSource.setDriverClassName(Preconditions.checkNotNull(this.env.getProperty("jdbc.driverClassName")));
        dataSource.setUrl(Preconditions.checkNotNull(this.env.getProperty("jdbc.url")));
        dataSource.setUsername(Preconditions.checkNotNull(this.env.getProperty("jdbc.user")));
        dataSource.setPassword(Preconditions.checkNotNull(this.env.getProperty("jdbc.pass")));

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
        sessionFactory.setPackagesToScan("com.dgc.dm.core");
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
        mailSender.setHost(this.env.getProperty("mail.host"));
        mailSender.setPort(Integer.parseInt(this.env.getProperty("mail.port")));
        mailSender.setUsername(getEncryptedProperty(this.env.getProperty("mail.username")));
        mailSender.setPassword(getEncryptedProperty(this.env.getProperty("mail.password")));

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", this.env.getProperty("mail.transport.protocol"));
        props.put("mail.smtp.auth", this.env.getProperty("mail.smtp.auth"));
        props.put("mail.smtp.starttls.enable", this.env.getProperty("mail.smtp.auth"));
        props.put("mail.debug", this.env.getProperty("mail.debug"));

        log.debug("[END] Mail Sender successfully configured ");
        return mailSender;
    }

    /**
     * Decrypt encryptedPropery
     *
     * @param encryptedPropery
     * @return decrypted propery
     */
    private String getEncryptedProperty(String encryptedPropery) {
        log.debug("[INIT] decrypting property {}", encryptedPropery);
        String decryptedProperty = null;
        try {
            decryptedProperty = PropertyValueEncryptionUtils.decrypt(encryptedPropery, getEncryptor());
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
     * @return
     */
    private PBEConfig getEnvironmentConfig() {
        log.debug("[INIT] getEnvironmentConfig");
        EnvironmentStringPBEConfig config = new EnvironmentStringPBEConfig();
        config.setAlgorithm(this.env.getProperty("jasypt.environment.algorithm"));
        config.setPasswordEnvName(this.env.getProperty("jasypt.environment.name"));
        log.debug("[END] getEnvironmentConfig");
        return config;
    }
}
