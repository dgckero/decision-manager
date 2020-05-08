/*
  @author david
 */

package com.dgc.dm.web.controller;

import com.dgc.dm.core.exception.DecisionException;
import com.dgc.dm.web.error.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.NoHandlerFoundException;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandlerController {

    /**
     * Generate a model ERROR adding message
     *
     * @param message
     * @return error view
     */
    private ModelAndView generateErrorModelAndView (final String message) {
        ModelAndView errorModelAndView = new ModelAndView(CommonController.ERROR_VIEW);
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
    public ModelAndView handleNullPointerException (Exception e) {
        log.error("Handle NullPointer exception {}", e);
        return generateErrorModelAndView("Ha ocurrido un error en el servidor, por favor póngase en contacto con el administrador");
    }

    /**
     * Handle DecisionException
     *
     * @param e
     * @return error view
     */
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(DecisionException.class)
    public ModelAndView handleDecisionException (Exception e) {
        log.error("handle DecisionException {}", e);
        return generateErrorModelAndView(e.getMessage());
    }

    /**
     * Handle Internal Server Error
     *
     * @param e
     * @return error view
     */
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public ModelAndView handleAllException (Exception e) {
        log.error("handle internal server error exception {}", e);
        return generateErrorModelAndView("Ha ocurrido un error en el servidor, por favor póngase en contacto con el administrador");
    }

    /**
     * Handle Resource Not Found Exception
     *
     * @return error view
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ModelAndView handleResourceNotFoundException ( ) {
        log.error("handle resource not found exception");
        return generateErrorModelAndView("El recurso al que intenta acceder no existe");
    }

    /**
     * Handle No Handler Found Exception
     *
     * @param ex
     * @return error view
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ModelAndView handle (NoHandlerFoundException ex) {
        log.error("handle page not found exception {}", ex.getMessage());
        return generateErrorModelAndView("La página a la que intenta acceder no existe");
    }
}
