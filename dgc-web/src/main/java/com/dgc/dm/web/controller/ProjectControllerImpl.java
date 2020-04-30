/*
  @author david
 */

package com.dgc.dm.web.controller;

import com.dgc.dm.core.dto.ProjectDto;
import com.dgc.dm.web.controller.iface.ProjectController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.Map;

@Slf4j
@Controller
public class ProjectControllerImpl extends CommonController implements ProjectController {

    /**
     * Go to new project view
     *
     * @return new project view
     */
    @Override
    public String newProject() {
        log.info("Go to new project view");
        return NEW_PROJECT_VIEW;
    }

    /**
     * Go to view project
     *
     * @return view project
     */
    @Override
    public ModelAndView selectProject() {
        log.info("[INIT] Go to view project view");
        final ModelAndView modelAndView = new ModelAndView(SELECT_PROJECT_VIEW);
        final Map<String, List<Map<String, Object>>> projects = this.getModelFacade().getExistingProjects();

        if (null == projects || projects.isEmpty()) {
            modelAndView.getModel().put("message", "No elements found that fit filters defined by user");
            modelAndView.setViewName(ERROR_VIEW);
        } else {
            log.info("Found {} projects", projects.size());
            modelAndView.addAllObjects(projects);
            modelAndView.addObject("selectedProject", ProjectDto.builder().build());
        }
        log.info("[END] Go to view project view");
        return modelAndView;
    }
}
