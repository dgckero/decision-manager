/*
  @author david
 */

package com.dgc.dm.web.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@Slf4j
@ControllerAdvice
class FileUploadExceptionAdvice {

    /**
     * Interceptor to handle max file size
     *
     * @param exc
     * @param request
     * @param response
     * @return error view
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ModelAndView handleMaxSizeException(final MaxUploadSizeExceededException exc, final HttpServletRequest request, final HttpServletResponse response) {
        log.debug("[INIT] handleMaxSizeException");
        final ModelAndView modelAndView = new ModelAndView("error");
        modelAndView.getModel().put("message", "File too large!");
        log.debug("[END] handleMaxSizeException");
        return modelAndView;
    }
}
