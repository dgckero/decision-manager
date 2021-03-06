/*
  @author david
 */

package com.dgc.dm.web.controller;

import com.dgc.dm.core.exception.DecisionException;
import com.dgc.dm.web.error.ResourceNotFoundException;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.NoHandlerFoundException;

@Log4j2
@ControllerAdvice
public class GlobalExceptionHandlerController {

    public static final String GENERIC_SERVER_ERROR = "Ha ocurrido un error en el servidor, por favor póngase en contacto con el administrador";
    public static final String RESOURCE_NOT_FOUND = "El recurso al que intenta acceder no existe";
    public static final String PAGE_NOT_FOUND = "La página a la que intenta acceder no existe";

    /**
     * Generate a model ERROR adding message
     *
     * @param message
     * @return error view
     */
    private ModelAndView generateErrorModelAndView(String message) {
        final ModelAndView errorModelAndView = new ModelAndView(CommonController.ERROR_VIEW);
        errorModelAndView.getModel().put(CommonController.MODEL_MESSAGE, message);
        return errorModelAndView;
    }

    /**
     * Handle NullPoinerException
     *
     * @param e
     * @return error view
     */
    @ExceptionHandler(NullPointerException.class)
    public ModelAndView handleNullPointerException(final Exception e) {
        log.error("Handle NullPointer exception {}", e.getMessage());
        return this.generateErrorModelAndView(GENERIC_SERVER_ERROR);
    }

    /**
     * Handle DecisionException
     *
     * @param e
     * @return error view
     */
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(DecisionException.class)
    public ModelAndView handleDecisionException(final Exception e) {
        log.error("handle DecisionException {}", e.getMessage());
        return this.generateErrorModelAndView(e.getMessage());
    }

    /**
     * Handle Internal Server Error
     *
     * @param e
     * @return error view
     */
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public ModelAndView handleAllException(final Exception e) {
        log.error("handle internal server error exception {}", e.getMessage());
        return this.generateErrorModelAndView(GENERIC_SERVER_ERROR);
    }

    /**
     * Handle Resource Not Found Exception
     *
     * @return error view
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ModelAndView handleResourceNotFoundException() {
        log.error("handle resource not found exception");
        return this.generateErrorModelAndView(RESOURCE_NOT_FOUND);
    }

    /**
     * Handle No Handler Found Exception
     *
     * @param ex
     * @return error view
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ModelAndView handle(final NoHandlerFoundException ex) {
        log.error("handle page not found exception {}", ex.getMessage());
        return this.generateErrorModelAndView(PAGE_NOT_FOUND);
    }
}
