/*
  @author david
 */
package com.dgc.dm.web.configuration;

import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

public class WebInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {

    /**
     * Specify Configuration classes for the root application context}
     *
     * @return null
     */
    @Override
    protected Class<?>[] getRootConfigClasses() {
        return null;
    }

    /**
     * Specify Configuration for the Servlet application context}
     *
     * @return ApplicationConfiguration
     */
    @Override
    protected Class<?>[] getServletConfigClasses() {
        return new Class[]{ApplicationConfiguration.class};
    }

    /**
     * Specify the servlet mapping(s) for Servlet
     *
     * @return ServletMapping
     */
    @Override
    protected String[] getServletMappings() {
        return new String[]{"/"};
    }

}
