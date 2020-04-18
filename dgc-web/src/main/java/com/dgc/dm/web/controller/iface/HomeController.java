/*
  @author david
 */

package com.dgc.dm.web.controller.iface;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

public interface HomeController extends HandlerExceptionResolver {
    @RequestMapping("/")
    ModelAndView home();
}
