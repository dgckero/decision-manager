/*
  @author david
 */

package com.dgc.dm.web.controller;


import com.dgc.dm.core.dto.ProjectDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
@Controller
public class ViewProjectController implements HandlerExceptionResolver {

    @PostMapping("/viewProject")
    public ModelAndView viewProject(@ModelAttribute("selectedProject") final ProjectDto selectedProject) {
        log.info("Selected project: " + selectedProject);

        ModelAndView modelAndView = new ModelAndView("viewProject");
        modelAndView.addObject("selectedProject", selectedProject);

        return modelAndView;
    }

    @Override
    public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response,
                                         Object object, Exception exc) {

        ModelAndView modelAndView = new ModelAndView("home");
        modelAndView.getModel().put("message", exc.getMessage());

        log.error("Error " + exc.getMessage());
        exc.printStackTrace();

        return modelAndView;

    }
}
