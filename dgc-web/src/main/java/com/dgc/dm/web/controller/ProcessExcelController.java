/*
  @author david
 */

package com.dgc.dm.web.controller;

import com.dgc.dm.core.dto.ProjectDto;
import com.dgc.dm.web.service.ExcelFacade;
import com.dgc.dm.web.service.ModelFacade;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
public class ProcessExcelController implements HandlerExceptionResolver {

    @Autowired
    private ExcelFacade excelFacade;
    @Autowired
    private ModelFacade modelFacade;

    @Override
    public final ModelAndView resolveException(final HttpServletRequest request, final HttpServletResponse response,
                                               final Object object, final Exception exc) {
        final ModelAndView modelAndView = new ModelAndView("file");
        if (exc instanceof MaxUploadSizeExceededException) {
            modelAndView.getModel().put("message", "File size exceeds limit!");
        }
        return modelAndView;
    }

    @RequestMapping(value = "/uploadFile", method = RequestMethod.POST)
    public final ModelAndView uploadFile(@RequestParam("projectName") final String projectName, @RequestParam("file") final MultipartFile file) {

        log.info("processing file {} for {}", file.getOriginalFilename(), projectName);

        final ModelAndView modelAndView = new ModelAndView("decision");

        ProjectDto project = excelFacade.processExcel(file, projectName);

        if (null == project) {
            log.error("Error creating project");
            modelAndView.setViewName("error");
            modelAndView.getModel().put("message", "Error creating project");
        } else {
            modelAndView.getModel().put("project", project);
            modelAndView.getModel().put("message", "File uploaded successfully!");
            Map<String, List<Map<String, Object>>> filters = getFiltersModelMap(project);
            if (null == filters || filters.isEmpty()) {
                log.error("No filters found");
                modelAndView.setViewName("error");
                modelAndView.getModel().put("message", "No filters found");
            } else {
                modelAndView.addAllObjects(filters);
                modelAndView.addObject("form", modelFacade.getFilterCreationDto(project, filters.get("filterList")));
                modelAndView.addObject("contactFilter", modelFacade.getContactFilter(project));
            }
        }
        return modelAndView;
    }

    private Map<String, List<Map<String, Object>>> getFiltersModelMap(ProjectDto project) {
        List<Map<String, Object>> filterList = modelFacade.getFilters(project);

        if (null == filterList || filterList.isEmpty()) {
            log.error("No filters found");
            return null;
        } else {
            log.info("found {} filters", filterList.size());
            final Map<String, List<Map<String, Object>>> modelMap = new HashMap<>();
            modelMap.put("filterList", filterList);
            return modelMap;
        }
    }

}
