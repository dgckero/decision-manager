/*
  @author david
 */

package com.dgc.dm.web.controller;

import com.dgc.dm.web.service.ExcelFacade;
import com.dgc.dm.web.service.ModelFacade;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.multipart.support.ByteArrayMultipartFileEditor;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
@Getter(AccessLevel.PROTECTED)
public class CommonController implements HandlerExceptionResolver {

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
    public final ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response,
                                               Object object, Exception exc) {
        ModelAndView modelAndView = new ModelAndView(ERROR_VIEW);
        modelAndView.getModel().put("message", exc.getMessage());

        log.error("Error {}", exc.getMessage());
        exc.printStackTrace();

        return modelAndView;
    }

    @InitBinder
    protected final void initBinder(ServletRequestDataBinder binder) {
        log.debug("initBinder convert multipart object to byte[]");
        binder.registerCustomEditor(byte[].class, new ByteArrayMultipartFileEditor());
    }
}
