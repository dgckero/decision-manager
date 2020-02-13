/**
 * @author david
 */

package com.dgc.dm.web.configuration;


import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

/**
 * @author david
 */
@Log4j2
@Configuration
@EnableWebMvc
@ComponentScan(basePackages = "com.dgc.dm.web")
public class ApplicationConfiguration implements WebMvcConfigurer {

    @Override
    public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
        configurer.enable();
    }

    @Bean
    public ResourceBundleMessageSource resourceBundleMessageSource() {
        log.debug("init resourceBundleMessageSource");
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("messages");
        log.debug("end resourceBundleMessageSource");
        return messageSource;
    }

    @Bean
    public InternalResourceViewResolver htmlViewResolver() {
        log.debug("init htmlViewResolver");
        InternalResourceViewResolver bean = new InternalResourceViewResolver();
        bean.setPrefix("/WEB-INF/views/");
        bean.setSuffix(".html");
        bean.setOrder(1);
        log.debug("end htmlViewResolver");
        return bean;
    }

    @Bean
    public MultipartResolver multipartResolver() {
        log.debug("init multipartResolver");
        CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver();
        // Max upload size 5MB
        multipartResolver.setMaxUploadSize(5242880);
        log.debug("end multipartResolver");
        return multipartResolver;
    }
}
