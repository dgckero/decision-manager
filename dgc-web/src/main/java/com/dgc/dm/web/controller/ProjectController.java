/*
  @author david
 */

package com.dgc.dm.web.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Slf4j
@Controller
public class ProjectController {
    @RequestMapping(value = "/project", method = RequestMethod.POST, params = "action=newProject")
    public ModelAndView newProject() {
        log.info("Go to new project view");
        return new ModelAndView("newProject");
    }

    @RequestMapping(value = "/project", method = RequestMethod.POST, params = "action=editProject")
    public ModelAndView editProject() {
        log.info("Go to edit project view");
        return new ModelAndView("editProject");
    }
}
