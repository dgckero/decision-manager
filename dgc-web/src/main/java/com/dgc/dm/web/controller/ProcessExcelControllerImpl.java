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

    @Override
    public ModelAndView uploadFile(String projectName, MultipartFile file) {
        log.info("processing file {} for {}", file.getOriginalFilename(), projectName);
        final ModelAndView modelAndView = new ModelAndView(CommonController.DECISION_VIEW);

        ProjectDto project = getExcelFacade().processExcel(file, projectName);

        if (null == project) {
            log.error("Error creating project");
            modelAndView.setViewName(CommonController.ERROR_VIEW);
            modelAndView.getModel().put("message", "Error creating project");
        } else {
            modelAndView.getModel().put("project", project);
            this.getModelFacade().addFilterInformationToModel(modelAndView, project);
        }
        return modelAndView;
    }
}
