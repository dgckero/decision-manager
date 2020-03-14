/*
  @author david
 */

package com.dgc.dm.web.configuration;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.config.annotation.*;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.thymeleaf.spring5.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.spring5.view.ThymeleafViewResolver;

@Configuration
@Slf4j
@EnableWebMvc
@ComponentScan({"com.dgc.dm"})
public class ApplicationConfiguration implements WebMvcConfigurer {

    @Autowired
    private ApplicationContext applicationContext;

    @Override
    public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
        configurer.enable();
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/resources/**")
                .addResourceLocations("/resources/");
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
    public SpringResourceTemplateResolver templateResolver() {
        log.debug("init templateResolver");

        SpringResourceTemplateResolver templateResolver = new SpringResourceTemplateResolver();
        templateResolver.setApplicationContext(applicationContext);
        templateResolver.setPrefix("/WEB-INF/views/");
        templateResolver.setSuffix(".html");

        log.debug("end templateResolver");
        return templateResolver;
    }

    @Bean
    public SpringTemplateEngine templateEngine() {
        SpringTemplateEngine templateEngine = new SpringTemplateEngine();
        templateEngine.setTemplateResolver(templateResolver());
        templateEngine.setEnableSpringELCompiler(true);
        return templateEngine;
    }

    @Override
    public void configureViewResolvers(ViewResolverRegistry registry) {
        ThymeleafViewResolver resolver = new ThymeleafViewResolver();
        resolver.setTemplateEngine(templateEngine());
        registry.viewResolver(resolver);
    }
//
//    @Bean
//    public InternalResourceViewResolver htmlViewResolver() {
//        log.debug("init htmlViewResolver");
//        InternalResourceViewResolver bean = new InternalResourceViewResolver();
//        bean.setPrefix("/WEB-INF/views/");
//        bean.setSuffix(".html");
//        bean.setOrder(1);
//        log.debug("end htmlViewResolver");
//        return bean;
//    }

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
