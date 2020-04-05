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

        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration()
                .setMatchingStrategy(MatchingStrategies.STRICT);

        log.debug("modelMapper successfully configured");

        return modelMapper;
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        final LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource());
        em.setPackagesToScan("com.dgc.dm.core");

        final JpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);
        em.setJpaProperties(additionalProperties());

        return em;
    }

    @Bean
    public DataSource dataSource() {
        final DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(Preconditions.checkNotNull(env.getProperty("jdbc.driverClassName")));
        dataSource.setUrl(Preconditions.checkNotNull(env.getProperty("jdbc.url")));
        dataSource.setUsername(Preconditions.checkNotNull(env.getProperty("jdbc.user")));
        dataSource.setPassword(Preconditions.checkNotNull(env.getProperty("jdbc.pass")));
        return dataSource;
    }

    @Bean
    public PlatformTransactionManager transactionManager(final EntityManagerFactory emf) {
        final JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(emf);
        return transactionManager;
    }

    @Bean
    public PersistenceExceptionTranslationPostProcessor exceptionTranslation() {
        return new PersistenceExceptionTranslationPostProcessor();
    }

    final Properties additionalProperties() {
        final Properties hibernateProperties = new Properties();
        hibernateProperties.setProperty("hibernate.dialect", env.getProperty("hibernate.dialect"));
        hibernateProperties.setProperty("hibernate.show_sql", env.getProperty("hibernate.show_sql"));

        return hibernateProperties;
    }

    @Bean
    public JdbcTemplate jdbcTemplate() {
        return new JdbcTemplate(dataSource());
    }

    @Bean
    public JavaMailSender getJavaMailSender() {
        log.info("Configuring Mail Sender");

        final JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(env.getProperty("mail.host"));
        mailSender.setPort(Integer.parseInt(env.getProperty("mail.port")));

        mailSender.setUsername(String.valueOf(this.getEncryptedProperty(env.getProperty("mail.username"))));
        mailSender.setPassword(String.valueOf(this.getEncryptedProperty(env.getProperty("mail.password"))));

        final Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", env.getProperty("mail.transport.protocol"));
        props.put("mail.smtp.auth", env.getProperty("mail.smtp.auth"));
        props.put("mail.smtp.starttls.enable", env.getProperty("mail.smtp.auth"));
        props.put("mail.debug", env.getProperty("mail.debug"));

        log.info("Mail Sender successfully configured ");

        return mailSender;
    }

    private Object getEncryptedProperty(final String encryptedPropery) {
        return PropertyValueEncryptionUtils.decrypt(encryptedPropery, this.getEncryptor());
    }

    private StandardPBEStringEncryptor getEncryptor() {
        final StandardPBEStringEncryptor configurationEncryptor = new StandardPBEStringEncryptor();
        configurationEncryptor.setConfig(this.getEnvironmentConfig());

        return configurationEncryptor;
    }

    private PBEConfig getEnvironmentConfig() {
        final EnvironmentStringPBEConfig config = new EnvironmentStringPBEConfig();
        config.setAlgorithm(env.getProperty("jasypt.environment.algorithm"));
        config.setPasswordEnvName(env.getProperty("jasypt.environment.name"));

        return config;
    }
}
