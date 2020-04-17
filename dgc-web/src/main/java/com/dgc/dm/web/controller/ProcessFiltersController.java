/*
  @author david
 */

package com.dgc.dm.web.controller;

import com.dgc.dm.core.db.service.DbServer;
import com.dgc.dm.core.dto.FilterCreationDto;
import com.dgc.dm.core.dto.FilterDto;
import com.dgc.dm.core.dto.ProjectDto;
import com.dgc.dm.core.service.bpmn.BPMNServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequestMapping("/filterList")
public class ProcessFiltersController implements HandlerExceptionResolver {

    @Autowired
    DbServer dbServer;

    @Autowired
    BPMNServer bpmnServer;

    private static boolean sendEmailToContact(List<FilterDto> filters) {
        final boolean sendEmail = filters.stream()
                .anyMatch(flt -> (null != flt.getContactFilter() && flt.getContactFilter().equals(Boolean.TRUE)));

        log.info("Send email enabled: {}", sendEmail);
        return sendEmail;
    }

    private static List<FilterDto> getActiveFilters(List<FilterDto> filters) {
        List<FilterDto> activeFilters = filters.stream()
                .filter(flt -> (null != flt.getActive() && flt.getActive().equals(Boolean.TRUE)))
                .collect(Collectors.toList());

        if (activeFilters.isEmpty()) {
            log.warn("No active filters found");
        } else {
            log.info("Found {} active filters", activeFilters.size());
            log.info("Active filters {}", activeFilters);
        }
        return activeFilters;
    }

    @PostMapping("/process")
    public final ModelAndView processFilters(@ModelAttribute FilterCreationDto form, @RequestParam(required = false, name = "emailTemplate") String emailTemplate) {

        ModelAndView modelAndView = new ModelAndView("result");

        try {
            List<FilterDto> filters = form.getFilters();
            log.info("Got filters {}", filters);

            if (filters.isEmpty()) {
                log.warn("No filters found");
                modelAndView.getModel().put("message", "No filters found");
            } else {
                List<FilterDto> activeFilters = getActiveFilters(filters);

                if (activeFilters.isEmpty()) {
                    modelAndView.getModel().put("message", "No Active filters found");
                } else {
                    ProjectDto project = filters.get(0).getProject();

                    if (null != emailTemplate) {
                        log.info("Adding emailTemplate {} to project {}", emailTemplate, project);
                        project.setEmailTemplate(emailTemplate);
                        this.dbServer.updateProject(project);
                    }

                    dbServer.updateFilters(activeFilters);

                    List<Map<String, Object>> result = bpmnServer.createBPMNModel(project, activeFilters, true, sendEmailToContact(filters));

                    if (result.isEmpty()) {
                        log.warn("No elements found that fit filters defined by user");
                        modelAndView.getModel().put("message", "No elements found that fit filters defined by user");
                    } else {
                        modelAndView.addObject("form", result);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error {}", e.getMessage());
            e.printStackTrace();
        }

        return modelAndView;
    }

    @Override
    public final ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response,
                                               Object object, Exception exc) {

        ModelAndView modelAndView = new ModelAndView("decision");

        modelAndView.getModel().put("message", exc.getMessage());
        return modelAndView;

    }
}
