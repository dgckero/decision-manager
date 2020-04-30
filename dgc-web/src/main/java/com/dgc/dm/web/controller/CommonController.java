/*
  @author david
 */

package com.dgc.dm.web.controller;

import com.dgc.dm.web.facade.ExcelFacade;
import com.dgc.dm.web.facade.ModelFacade;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.multipart.support.ByteArrayMultipartFileEditor;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.SimpleDateFormat;
import java.util.Date;

@Slf4j
@Getter(AccessLevel.PROTECTED)
class CommonController implements HandlerExceptionResolver {

    protected static final String DECISION_VIEW = "decision";
    protected static final String PROJECT_VIEW = "project";
    protected static final String VIEW_PROJECT = "viewProject";
    protected static final String ERROR_VIEW = "error";
    protected static final String SUCCESS_VIEW = "success";
    protected static final String RESULT_VIEW = "result";
    protected static final String HOME_VIEW = "home";
    protected static final String SELECT_PROJECT_VIEW = "selectProject";
    protected static final String NEW_PROJECT_VIEW = "newProject";

    protected final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

    @Autowired
    private ExcelFacade excelFacade;
    @Autowired
    private ModelFacade modelFacade;

    /**
     * Resolve the given exception that got thrown during handler execution returning ModelAndView to ERROR_VIEW
     *
     * @param request
     * @param response
     * @param object
     * @param exc
     * @return ModelAndView
     */
    @Override
    public final ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response,
                                               Object object, Exception exc) {
        log.debug("[INIT] resolveException");
        ModelAndView modelAndView = new ModelAndView(ERROR_VIEW);
        modelAndView.getModel().put("message", exc.getMessage());
        log.error("Error {}", (exc.getCause() == null) ? exc.getMessage() : exc.getCause().getMessage());
        log.debug("[END] resolveException");
        return modelAndView;
    }

    /**
     * Ad binding on byte array and date types on initialization of the WebDataBinder which
     * will be used for populating command and form object arguments
     * of annotated handler methods.
     *
     * @param binder
     */
    @InitBinder
    protected final void initBinder(ServletRequestDataBinder binder) {
        log.debug("[INIT] initBinder registering custom property editor for byte[] and Date");
        binder.registerCustomEditor(byte[].class, new ByteArrayMultipartFileEditor());
        binder.registerCustomEditor(Date.class, new CustomDateEditor(format, true));
        log.debug("[END] initBinder ");
    }
}
