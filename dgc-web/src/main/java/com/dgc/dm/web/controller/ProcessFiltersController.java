/*
  @author david
 */

package com.dgc.dm.web.controller;

import com.dgc.dm.core.bpmn.BPMNServer;
import com.dgc.dm.core.db.service.DbServer;
import com.dgc.dm.core.dto.FilterCreationDto;
import com.dgc.dm.core.dto.FilterDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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
    public ModelAndView processFilters(@ModelAttribute FilterCreationDto form) throws Exception {

        ModelAndView modelAndView = new ModelAndView("result");

        try {
            List<FilterDto> filters = form.getFilters();
            log.info("Got filters " + filters);

            if (filters.isEmpty()) {
                log.warn("No filters found");
                modelAndView.getModel().put("message", "No filters found");
            } else {
                List<FilterDto> activeFilters = getActiveFilters(filters);

                if (activeFilters.isEmpty()) {
                    modelAndView.getModel().put("message", "No Active filters found");
                } else {
                    dbServer.updateFilters(activeFilters);

                    List<Map<String, Object>> result = bpmnServer.createBPMNModel(activeFilters, true);

                    if (result.isEmpty()) {
                        log.warn("No elements found that fit filters defined by user");
                        modelAndView.getModel().put("message", "No elements found that fit filters defined by user");
                    } else {
                        modelAndView.addObject("form", result);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error " + e.getMessage());
            e.printStackTrace();
        }

        return modelAndView;
    }

    private List<FilterDto> getActiveFilters(List<FilterDto> filters) {
        List<FilterDto> activeFilters = filters.stream()
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
    public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response,
                                         Object object, Exception exc) {

        ModelAndView modelAndView = new ModelAndView("decision");

        modelAndView.getModel().put("message", exc.getMessage());
        return modelAndView;

    }
}
