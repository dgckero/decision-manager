/*
  @author david
 */

package com.dgc.dm.web.controller.iface;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

public interface ProjectController extends HandlerExceptionResolver {
    @RequestMapping(value = "/project", method = RequestMethod.POST, params = "action=newProject")
    String newProject();

    @RequestMapping(value = "/project", method = RequestMethod.POST, params = "action=selectProject")
    ModelAndView selectProject();
}
