/*
  @author david
 */

package com.dgc.dm.web.controller.iface;

import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;


public interface ViewProjectController extends HandlerExceptionResolver {
    @PostMapping("/viewProject")
    ModelAndView viewProject(@ModelAttribute("selectedProjectId") Integer selectedProjectId);
}
