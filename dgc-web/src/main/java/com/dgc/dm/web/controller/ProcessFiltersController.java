/*
  @author david
 */

package com.dgc.dm.web.controller;

import com.dgc.dm.core.bpmn.BPMNServer;
import com.dgc.dm.core.db.service.DbServer;
import com.dgc.dm.core.dto.FilterCreationDto;
import com.dgc.dm.core.dto.FilterDto;
import com.dgc.dm.core.dto.ProjectDto;
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

    @PostMapping("/process")
    public ModelAndView processFilters(@ModelAttribute final FilterCreationDto form, @RequestParam(required = false, name = "emailTemplate") final String emailTemplate) {

        final ModelAndView modelAndView = new ModelAndView("result");

        try {
            final List<FilterDto> filters = form.getFilters();
            log.info("Got filters " + filters);

            if (filters.isEmpty()) {
                log.warn("No filters found");
                modelAndView.getModel().put("message", "No filters found");
            } else {
                final List<FilterDto> activeFilters = this.getActiveFilters(filters);

                if (activeFilters.isEmpty()) {
                    modelAndView.getModel().put("message", "No Active filters found");
                } else {
                    final ProjectDto project = filters.get(0).getProject();

                    if (emailTemplate != null) {
                        log.info("Adding emailTemplate " + emailTemplate + " to project " + project);
                        project.setEmailTemplate(emailTemplate);
                        dbServer.updateProject(project);
                    }

                    this.dbServer.updateFilters(activeFilters);

                    final List<Map<String, Object>> result = this.bpmnServer.createBPMNModel(project, activeFilters, true, this.sendEmailToContact(filters));

                    if (result.isEmpty()) {
                        log.warn("No elements found that fit filters defined by user");
                        modelAndView.getModel().put("message", "No elements found that fit filters defined by user");
                    } else {
                        modelAndView.addObject("form", result);
                    }
                }
            }
        } catch (final Exception e) {
            log.error("Error " + e.getMessage());
            e.printStackTrace();
        }

        return modelAndView;
    }

    private boolean sendEmailToContact(final List<FilterDto> filters) {
        boolean sendEmail = filters.stream()
                .anyMatch(flt -> (flt.getContactFilter() != null && flt.getContactFilter().equals(Boolean.TRUE)));

        log.info("Send email enabled: " + sendEmail);
        return sendEmail;
    }

    private List<FilterDto> getActiveFilters(final List<FilterDto> filters) {
        final List<FilterDto> activeFilters = filters.stream()
                .filter(flt -> (flt.getActive() != null && flt.getActive().equals(Boolean.TRUE)))
                .collect(Collectors.toList());

        if (activeFilters.isEmpty()) {
            log.warn("No active filters found");
        } else {
            log.info("Found " + activeFilters.size() + " active filters");
            log.info("Active filters " + activeFilters);
        }
        return activeFilters;
    }

    @Override
    public ModelAndView resolveException(final HttpServletRequest request, final HttpServletResponse response,
                                         final Object object, final Exception exc) {

        final ModelAndView modelAndView = new ModelAndView("decision");

        modelAndView.getModel().put("message", exc.getMessage());
        return modelAndView;

    }
}
