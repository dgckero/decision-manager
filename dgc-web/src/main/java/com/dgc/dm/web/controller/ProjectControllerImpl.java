/*
  @author david
 */

package com.dgc.dm.web.controller;

import com.dgc.dm.core.dto.ProjectDto;
import com.dgc.dm.web.controller.iface.ProjectController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
public class ProjectControllerImpl extends CommonController implements ProjectController {

    @Override
    public String newProject() {
        log.info("Go to new project view");
        return NEW_PROJECT_VIEW;
    }

    @Override
    public ModelAndView selectProject() {
        log.info("Go to view project view");

        ModelAndView modelAndView = new ModelAndView(SELECT_PROJECT_VIEW);
        Map<String, List<Map<String, Object>>> projects = getExistingProjects();

        if (null == projects || projects.isEmpty()) {
            modelAndView.getModel().put("message", "No elements found that fit filters defined by user");
            modelAndView.setViewName(ERROR_VIEW);
        } else {
            log.info("Found {} projects", projects.size());
            modelAndView.addAllObjects(projects);
            modelAndView.addObject("selectedProject", ProjectDto.builder().build());
        }

        return modelAndView;
    }

    private Map<String, List<Map<String, Object>>> getExistingProjects() {
        List<Map<String, Object>> projects = getModelFacade().getProjects();
        if (projects == null) {
            log.warn("No projects founds");
            return null;
        } else {
            log.info("Found {} projects", projects.size());
            Map<String, List<Map<String, Object>>> modelMap = new HashMap<>();
            modelMap.put("existingProjects", projects);
            return modelMap;
        }
    }
}
