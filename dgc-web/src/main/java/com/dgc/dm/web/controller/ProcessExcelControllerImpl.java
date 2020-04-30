/*
  @author david
 */

package com.dgc.dm.web.controller;

import com.dgc.dm.core.dto.ProjectDto;
import com.dgc.dm.web.controller.iface.ProcessExcelController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

@Slf4j
@Controller
public class ProcessExcelControllerImpl extends CommonController implements ProcessExcelController {

    /**
     * Process Excel file and create project model
     *
     * @param projectName
     * @param file
     * @return decision view
     */
    @Override
    public ModelAndView uploadFile(final String projectName, final MultipartFile file) {
        log.info("[INIT] processing file {} for {}", file.getOriginalFilename(), projectName);
        ModelAndView modelAndView = new ModelAndView(CommonController.DECISION_VIEW);

        final ProjectDto project = this.getExcelFacade().processExcel(file, projectName);

        if (null == project) {
            log.error("Error creating project");
            modelAndView.setViewName(CommonController.ERROR_VIEW);
            modelAndView.getModel().put("message", "Error creating project");
        } else {
            modelAndView.getModel().put("project", project);
            getModelFacade().addFilterInformationToModel(modelAndView, project);
        }
        log.info("[END] processing file {} for {}", file.getOriginalFilename(), projectName);
        return modelAndView;
    }
}
