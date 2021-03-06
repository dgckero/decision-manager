/*
  @author david
 */

package com.dgc.dm.web.configuration;


import lombok.extern.log4j.Log4j2;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.*;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;
import org.springframework.web.servlet.theme.FixedThemeResolver;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.thymeleaf.spring5.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.spring5.view.ThymeleafViewResolver;

import java.util.Locale;

@Log4j2
@Configuration
@EnableWebMvc
@ComponentScan("com.dgc.dm")
public class ApplicationConfiguration implements WebMvcConfigurer {

    /**
     * Maximum allowed size (in bytes) before an upload gets rejected.
     */
    private static final long MAX_UPLOAD_SIZE = 5242880L;

    /**
     * Spanish locale
     */
    private static final Locale SPANISH_LOCALE = new Locale("es", "ES");
    private static final String RESOURCES_PATH = "/resources/**";
    private static final String RESOURCES_CLASSPATH = "classpath:/resources/";
    private static final String WEBJARS_PATH = "/webjars/**";
    private static final String WEBJARS_CLASSPATH = "classpath:/META-INF/resources/webjars/";
    private static final String IMAGES_PATH = "/images/**";
    private static final String IMAGES_CLASSPATH = "/images/";
    private static final String CSS_PATH = "/css/**";
    private static final String CSS_CLASSPATH = "/css/";
    private static final String I18_PATH = "/i18/**";
    private static final String I18_CLASSPATH = "/i18/";
    private static final String WEB_INF_VIEWS_PATH = "/WEB-INF/views/";
    private static final String HTML_EXTENSION = ".html";


    private final ApplicationContext applicationContext;

    /**
     * ApplicationConfiguration constructor
     * initialize applicationContext
     *
     * @param applicationContext
     */
    public ApplicationConfiguration(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    /**
     * Add a resource handler for serving static resources based on the specified URL path
     *
     * @param registry
     */

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        log.debug("[INIT] addResourceHandlers registry: {}", registry);
        registry.addResourceHandler(RESOURCES_PATH)
                .addResourceLocations(RESOURCES_CLASSPATH);
        registry.addResourceHandler(WEBJARS_PATH)
                .addResourceLocations(WEBJARS_CLASSPATH);
        registry.addResourceHandler(IMAGES_PATH)
                .addResourceLocations(IMAGES_CLASSPATH);
        registry.addResourceHandler(CSS_PATH)
                .addResourceLocations(CSS_CLASSPATH);
        registry.addResourceHandler(I18_PATH)
                .addResourceLocations(I18_CLASSPATH);
        log.debug("[END] addResourceHandlers");
    }

    /**
     * Adds the provided interceptorRegistry
     *
     * @param registry
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        log.debug("[INIT] addInterceptors registry: {}", registry);
        registry.addInterceptor(localeChangeInterceptor());
        log.debug("[END] addInterceptors");
    }

    /**
     * Interceptor that allows for changing the current locale on every request,
     * via a configurable request parameter (default parameter name: "locale").
     *
     * @return LocaleChangeInterceptor
     */
    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        log.debug("[INIT] localeChangeInterceptor");
        LocaleChangeInterceptor lci = new LocaleChangeInterceptor();
        lci.setParamName("lang");
        log.debug("[END] localeChangeInterceptor");
        return lci;
    }

    /**
     * Interface for web-based locale resolution strategies that allows for both locale resolution via the request and locale modification via request and response.
     *
     * @return SessionLocaleResolver, default locale Spanish
     */
    @Bean
    public LocaleResolver localeResolver() {
        log.debug("[INIT] localeResolver");
        SessionLocaleResolver slr = new SessionLocaleResolver();
        slr.setDefaultLocale(SPANISH_LOCALE);
        log.debug("[END] localeResolver");
        return slr;
    }

    /**
     * Set default web-based theme
     *
     * @return ThemeResolver
     */
    @Bean
    public FixedThemeResolver themeResolver() {
        log.debug("[INIT] themeResolver");
        FixedThemeResolver resolver = new FixedThemeResolver();
        resolver.setDefaultThemeName("default-theme");
        log.debug("[END] themeResolver");
        return resolver;
    }

    /**
     * Define MessageSource implementation that accesses resource bundles using specified basenames.
     *
     * @return ResourceBundleMessageSource
     */
    @Bean
    public ResourceBundleMessageSource resourceBundleMessageSource() {
        log.debug("[INIT] resourceBundleMessageSource");
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("messages");
        log.debug("[END] resourceBundleMessageSource");
        return messageSource;
    }

    /**
     * SpringResourceTemplateResolver automatically integrates with Spring's own
     * resource resolution infrastructure
     *
     * @return SpringResourceTemplateResolver
     */
    @Bean
    public SpringResourceTemplateResolver templateResolver() {
        log.debug("[INIT] templateResolver");

        SpringResourceTemplateResolver templateResolver = new SpringResourceTemplateResolver();
        templateResolver.setApplicationContext(applicationContext);
        templateResolver.setPrefix(WEB_INF_VIEWS_PATH);
        templateResolver.setSuffix(HTML_EXTENSION);

        log.debug("[END] templateResolver");
        return templateResolver;
    }

    /**
     * applies SpringStandardDialect and enables Spring's own MessageSource message resolution mechanisms
     *
     * @return SpringTemplateEngine
     */
    @Bean
    public SpringTemplateEngine templateEngine() {
        log.debug("[INIT] templateEngine");
        SpringTemplateEngine templateEngine = new SpringTemplateEngine();
        templateEngine.setTemplateResolver(templateResolver());
        templateEngine.setEnableSpringELCompiler(true);
        log.debug("[END] templateEngine");
        return templateEngine;
    }

    /**
     * Configure view resolvers to translate String-based view names returned from controllers into concrete View implementations to perform rendering with.
     *
     * @param registry
     */
    @Override
    public void configureViewResolvers(ViewResolverRegistry registry) {
        log.debug("[INIT] configureViewResolvers registry: {}", registry);
        ThymeleafViewResolver resolver = new ThymeleafViewResolver();
        resolver.setTemplateEngine(templateEngine());
        registry.viewResolver(resolver);
        log.debug("[END] configureViewResolvers");
    }

    /**
     * Set the maximum allowed size (in bytes) before an upload gets rejected.
     *
     * @return MultipartResolver
     */
    @Bean
    public MultipartResolver multipartResolver() {
        log.debug("[INIT] multipartResolver");
        CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver();
        multipartResolver.setMaxUploadSize(MAX_UPLOAD_SIZE);
        log.debug("[END] multipartResolver");
        return multipartResolver;
    }

}