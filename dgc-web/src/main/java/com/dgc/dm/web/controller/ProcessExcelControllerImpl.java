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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
public class ProcessExcelControllerImpl extends CommonController implements ProcessExcelController {

    @Override
    public ModelAndView uploadFile(final String projectName, final MultipartFile file) {
        log.info("processing file {} for {}", file.getOriginalFilename(), projectName);

        ModelAndView modelAndView = new ModelAndView(CommonController.DECISION_VIEW);

        final ProjectDto project = this.getExcelFacade().processExcel(file, projectName);

        if (null == project) {
            log.error("Error creating project");
            modelAndView.setViewName(CommonController.ERROR_VIEW);
            modelAndView.getModel().put("message", "Error creating project");
        } else {
            modelAndView.getModel().put("project", project);
            addFilterInformationToModel(modelAndView, project);
        }
        return modelAndView;
    }

    private void addFilterInformationToModel(ModelAndView modelAndView, ProjectDto project) {
        final Map<String, List<Map<String, Object>>> filters = this.getFiltersModelMap(project);

        if (null == filters || filters.isEmpty()) {
            log.error("No filters found");
            modelAndView.setViewName(ERROR_VIEW);
            modelAndView.getModel().put("message", "No filters found");
        } else {
            log.error("Found " + filters.size() + " filters");
            modelAndView.addAllObjects(filters);
            modelAndView.addObject("form", this.getModelFacade().getFilterCreationDto(project, filters.get("filterList")));
            modelAndView.addObject("contactFilter", this.getModelFacade().getContactFilter(project));
        }
    }

    private Map<String, List<Map<String, Object>>> getFiltersModelMap(final ProjectDto project) {
        final Map<String, List<Map<String, Object>>> result;
        final List<Map<String, Object>> filterList = this.getModelFacade().getFilters(project);

        if (null == filterList || filterList.isEmpty()) {
            log.error("No filters found");
            result = null;
        } else {
            log.info("found {} filters", filterList.size());
            Map<String, List<Map<String, Object>>> modelMap = new HashMap<>();
            modelMap.put("filterList", filterList);
            result = modelMap;
        }
        return result;
    }

}
