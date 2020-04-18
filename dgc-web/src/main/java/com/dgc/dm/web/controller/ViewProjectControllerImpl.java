/*
  @author david
 */

package com.dgc.dm.web.controller;


import com.dgc.dm.core.dto.ProjectDto;
import com.dgc.dm.web.controller.iface.ViewProjectController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.ModelAndView;

@Slf4j
@Controller
public class ViewProjectControllerImpl extends CommonController implements ViewProjectController {

    @Override
    public ModelAndView viewProject(@ModelAttribute("selectedProjectId") final Integer selectedProjectId) {
        ModelAndView modelAndView = new ModelAndView("viewProject");
        if (selectedProjectId == null) {
            log.error("SelectedProjectId is NULL");
        } else {
            log.info("SelectedProjectId " + selectedProjectId);
            ProjectDto selectedProject = getModelFacade().getProject(selectedProjectId);

            if (null == selectedProject) {
                log.error("Project not found with ID " + selectedProjectId);
                modelAndView.addObject("message", "Project NOT found");
            } else {
                log.info("Got project " + selectedProject);
                modelAndView.addObject("selectedProject", selectedProject);
            }
        }
        return modelAndView;
    }

}
