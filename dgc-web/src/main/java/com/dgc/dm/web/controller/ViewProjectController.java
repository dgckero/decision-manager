/*
  @author david
 */

package com.dgc.dm.web.controller;


import com.dgc.dm.core.db.service.DbServer;
import com.dgc.dm.core.dto.ProjectDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    DbServer dbServer;

    @PostMapping("/viewProject")
    public ModelAndView viewProject(@ModelAttribute("selectedProjectId") final Integer selectedProjectId) {
        ModelAndView modelAndView = new ModelAndView("viewProject");
        if (selectedProjectId == null) {
            log.error("SelectedProjectId is NULL");
        } else {
            log.info("SelectedProjectId " + selectedProjectId);
            ProjectDto selectedProject = dbServer.getProject(selectedProjectId);

            if (selectedProject == null) {
                log.error("Project not found with ID " + selectedProjectId);
                modelAndView.addObject("message", "Project NOT found");
            } else {
                log.info("Got project " + selectedProject);
                modelAndView.addObject("selectedProject", selectedProject);
            }
        }
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
