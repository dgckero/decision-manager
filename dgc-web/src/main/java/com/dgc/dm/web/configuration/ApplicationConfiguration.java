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
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.*;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;
import org.springframework.web.servlet.theme.FixedThemeResolver;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.thymeleaf.spring5.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.spring5.view.ThymeleafViewResolver;

import java.util.Locale;

@Slf4j
@Configuration
@EnableWebMvc
@ComponentScan("com.dgc.dm")
public class ApplicationConfiguration implements WebMvcConfigurer {

    /**
     * Maximum allowed size (in bytes) before an upload gets rejected.
     */
    private static final long MAX_UPLOAD_SIZE = 5242880L;

    private static final Locale SPANISH_LOCALE = new Locale("es", "ES");


    @Autowired
    private ApplicationContext applicationContext;

    @Override
    public void configureDefaultServletHandling(final DefaultServletHandlerConfigurer configurer) {
        configurer.enable();
    }

    @Override
    public void addResourceHandlers(final ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/resources/**")
                .addResourceLocations("classpath:/resources/");
        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/");
    }

    @Override
    public void addInterceptors(final InterceptorRegistry registry) {
        registry.addInterceptor(this.localeChangeInterceptor());
    }

    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        final LocaleChangeInterceptor lci = new LocaleChangeInterceptor();
        lci.setParamName("lang");
        return lci;
    }

    @Bean(DispatcherServlet.LOCALE_RESOLVER_BEAN_NAME)
    public LocaleResolver localeResolver() {
        final SessionLocaleResolver slr = new SessionLocaleResolver();
        slr.setDefaultLocale(SPANISH_LOCALE);
        return slr;
    }

    @Bean(DispatcherServlet.THEME_RESOLVER_BEAN_NAME)
    public FixedThemeResolver themeResolver() {
        final FixedThemeResolver resolver = new FixedThemeResolver();
        resolver.setDefaultThemeName("default-theme");
        return resolver;
    }

    @Bean
    public ResourceBundleMessageSource resourceBundleMessageSource() {
        log.debug("init resourceBundleMessageSource");
        final ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("messages");
        log.debug("end resourceBundleMessageSource");
        return messageSource;
    }

    @Bean
    public SpringResourceTemplateResolver templateResolver() {
        log.debug("init templateResolver");

        final SpringResourceTemplateResolver templateResolver = new SpringResourceTemplateResolver();
        templateResolver.setApplicationContext(this.applicationContext);
        templateResolver.setPrefix("/WEB-INF/views/");
        templateResolver.setSuffix(".html");

        log.debug("end templateResolver");
        return templateResolver;
    }

    @Bean
    public SpringTemplateEngine templateEngine() {
        final SpringTemplateEngine templateEngine = new SpringTemplateEngine();
        templateEngine.setTemplateResolver(this.templateResolver());
        templateEngine.setEnableSpringELCompiler(true);
        return templateEngine;
    }

    @Override
    public void configureViewResolvers(final ViewResolverRegistry registry) {
        final ThymeleafViewResolver resolver = new ThymeleafViewResolver();
        resolver.setTemplateEngine(this.templateEngine());
        registry.viewResolver(resolver);
    }

    @Bean
    public MultipartResolver multipartResolver() {
        log.debug("init multipartResolver");
        final CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver();
        multipartResolver.setMaxUploadSize(MAX_UPLOAD_SIZE);
        log.debug("end multipartResolver");
        return multipartResolver;
    }
}