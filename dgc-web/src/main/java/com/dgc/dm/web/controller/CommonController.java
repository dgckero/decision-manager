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

    protected final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

    protected static final String DECISION_VIEW = "decision";
    protected static final String PROJECT_VIEW = "project";
    protected static final String ERROR_VIEW = "error";
    protected static final String SUCCESS_VIEW = "success";
    protected static final String RESULT_VIEW = "result";
    protected static final String HOME_VIEW = "home";
    protected static final String SELECT_PROJECT_VIEW = "selectProject";
    protected static final String NEW_PROJECT_VIEW = "newProject";

    @Autowired
    private ExcelFacade excelFacade;
    @Autowired
    private ModelFacade modelFacade;

    @Override
    public final ModelAndView resolveException(final HttpServletRequest request, final HttpServletResponse response,
                                               final Object object, final Exception exc) {

        final ModelAndView modelAndView = new ModelAndView(ERROR_VIEW);
        modelAndView.getModel().put("message", exc.getMessage());
        log.error("Error {}", exc.getMessage());

        return modelAndView;
    }

    @InitBinder
    protected final void initBinder(final ServletRequestDataBinder binder) {
        log.debug("initBinder registering custom property editor for byte[] and Date");
        binder.registerCustomEditor(byte[].class, new ByteArrayMultipartFileEditor());
        binder.registerCustomEditor(Date.class, new CustomDateEditor(this.format, true));
    }
}
