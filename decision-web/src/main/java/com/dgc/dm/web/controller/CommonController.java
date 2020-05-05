/*
  @author david
 */

package com.dgc.dm.web.controller;

import com.dgc.dm.core.exception.DecisionException;
import com.dgc.dm.web.configuration.ApplicationConfiguration;
import com.dgc.dm.web.facade.ExcelFacade;
import com.dgc.dm.web.facade.ModelFacade;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
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

    protected static final String FILTERS_VIEW = "filters";
    protected static final String EDIT_PROJECT = "editProject";
    protected static final String ERROR_VIEW = "error";
    protected static final String SUCCESS_VIEW = "success";
    protected static final String RESULT_VIEW = "result";
    protected static final String HOME_VIEW = "home";
    protected static final String SELECT_PROJECT_VIEW = "selectProject";
    protected static final String NEW_PROJECT_VIEW = "newProject";
    protected static final String MODEL_MESSAGE = "message";

    protected final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

    @Autowired
    private ExcelFacade excelFacade;
    @Autowired
    private ModelFacade modelFacade;

    /**
     * Ad binding on byte array and date types on initialization of the WebDataBinder which
     * will be used for populating command and form object arguments
     * of annotated handler methods.
     *
     * @param binder
     */
    @InitBinder
    protected final void initBinder (final ServletRequestDataBinder binder) {
        log.debug("[INIT] initBinder registering custom property editor for byte[] and Date");
        binder.registerCustomEditor(byte[].class, new ByteArrayMultipartFileEditor());
        binder.registerCustomEditor(Date.class, new CustomDateEditor(this.format, true));
        log.debug("[END] initBinder ");
    }

    /**
     * Resolve the given exception that got thrown during handler execution returning ModelAndView to ERROR_VIEW
     *
     * @param request
     * @param response
     * @param object
     * @param exception
     * @return error view
     */
    @Override
    public final ModelAndView resolveException (final HttpServletRequest request, final HttpServletResponse response,
                                                final Object object, final Exception exception) {
        log.debug("[INIT] handleError exception: {}", exception);
        final ModelAndView modelAndView = new ModelAndView(ERROR_VIEW);

        log.error("Error {}", (exception.getCause() == null) ? exception.getMessage() : exception.getCause().getMessage());
        if (exception instanceof MaxUploadSizeExceededException) {
            modelAndView.getModel().put(MODEL_MESSAGE, "El fichero a procesar es demasiado grande, el tamaño máximo es: " + ApplicationConfiguration.MAX_UPLOAD_SIZE);
        } else {
            if (!(exception instanceof DecisionException)) {
                log.error("Uncontrolled Exception: {}", exception);
            }
            modelAndView.getModel().put(MODEL_MESSAGE, exception.getMessage());
        }
        log.debug("[END] resolveException exception: {}", exception);
        return modelAndView;
    }

}
