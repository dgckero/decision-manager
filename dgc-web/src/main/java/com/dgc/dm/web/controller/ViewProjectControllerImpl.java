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

    /**
     * Go to view project based on selectedProjectId
     *
     * @param selectedProjectId
     * @return view project
     */
    @Override
    public ModelAndView viewProject(@ModelAttribute("selectedProjectId") Integer selectedProjectId) {
        log.info("[INIT] viewProject by id: {}", selectedProjectId);
        final ModelAndView modelAndView = new ModelAndView(VIEW_PROJECT);
        if (selectedProjectId == null) {
            log.error("SelectedProjectId is NULL");
        } else {
            log.info("SelectedProjectId " + selectedProjectId);
            final ProjectDto selectedProject = this.getModelFacade().getProject(selectedProjectId);

            if (null == selectedProject) {
                log.error("Project not found with ID " + selectedProjectId);
                modelAndView.addObject("message", "Project NOT found");
            } else {
                log.info("Got project " + selectedProject);
                modelAndView.addObject("selectedProject", selectedProject);
            }
        }
        log.info("[END] viewProject by id: {}", selectedProjectId);
        return modelAndView;
    }

}
