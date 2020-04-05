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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.Properties;

@Slf4j
@Configuration
@EnableTransactionManagement
@PropertySource({
        "classpath:application.properties",
        "classpath:persistence.properties",
        "classpath:smtp.properties"
})
@ComponentScan("com.dgc.dm.core")
@EnableJpaRepositories(basePackages = "com.dgc.dm.core.db")
public class ApplicationConfig {

    @Autowired
    private Environment env;

    public ApplicationConfig() {
    }

    @Bean
    public ModelMapper modelMapper() {
        log.debug("Configuring modelMapper");

        final ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration()
                .setMatchingStrategy(MatchingStrategies.STRICT);

        log.debug("modelMapper successfully configured");

        return modelMapper;
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(this.dataSource());
        em.setPackagesToScan("com.dgc.dm.core");

        JpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);
        em.setJpaProperties(this.additionalProperties());

        return em;
    }

    @Bean
    public DataSource dataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(Preconditions.checkNotNull(this.env.getProperty("jdbc.driverClassName")));
        dataSource.setUrl(Preconditions.checkNotNull(this.env.getProperty("jdbc.url")));
        dataSource.setUsername(Preconditions.checkNotNull(this.env.getProperty("jdbc.user")));
        dataSource.setPassword(Preconditions.checkNotNull(this.env.getProperty("jdbc.pass")));
        return dataSource;
    }

    @Bean
    public PlatformTransactionManager transactionManager(EntityManagerFactory emf) {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(emf);
        return transactionManager;
    }

    @Bean
    public PersistenceExceptionTranslationPostProcessor exceptionTranslation() {
        return new PersistenceExceptionTranslationPostProcessor();
    }

    final Properties additionalProperties() {
        Properties hibernateProperties = new Properties();
        hibernateProperties.setProperty("hibernate.dialect", this.env.getProperty("hibernate.dialect"));
        hibernateProperties.setProperty("hibernate.show_sql", this.env.getProperty("hibernate.show_sql"));

        return hibernateProperties;
    }

    @Bean
    public JdbcTemplate jdbcTemplate() {
        return new JdbcTemplate(this.dataSource());
    }

    @Bean
    public JavaMailSender getJavaMailSender() {
        log.info("Configuring Mail Sender");

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

        log.info("Mail Sender successfully configured ");
        return mailSender;
    }

    private String getEncryptedProperty(String encryptedPropery) {
        String decryptedProperty = null;
        try {
            decryptedProperty = PropertyValueEncryptionUtils.decrypt(encryptedPropery, getEncryptor());
        } catch (NullPointerException e) {
            log.error("JASYPT ENVIRONMENT VARIABLE HAS NOT BEEN DEFINED");
        }
        return decryptedProperty;
    }

    private StandardPBEStringEncryptor getEncryptor() {
        StandardPBEStringEncryptor standardPBEStringEncryptor = new StandardPBEStringEncryptor();
        standardPBEStringEncryptor.setConfig(getEnvironmentConfig());

        return standardPBEStringEncryptor;
    }

    private PBEConfig getEnvironmentConfig() {
        EnvironmentStringPBEConfig config = new EnvironmentStringPBEConfig();
        config.setAlgorithm(this.env.getProperty("jasypt.environment.algorithm"));
        config.setPasswordEnvName(this.env.getProperty("jasypt.environment.name"));

        return config;
    }
}
