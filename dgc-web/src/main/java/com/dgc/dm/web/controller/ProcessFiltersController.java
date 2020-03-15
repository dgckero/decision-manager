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
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
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
    public String processFilters(@ModelAttribute FilterCreationDto form, Model model) throws Exception {

        List<FilterDto> filters = form.getFilters();

        log.info("Got filters " + filters);
        List<FilterDto> activeFilters = filters.stream()
                .filter(flt -> (flt.getActive() != null && flt.getActive().equals(Boolean.TRUE)))
                .collect(Collectors.toList());
        log.info("Active filters " + activeFilters);

        dbServer.updateFilters(activeFilters);
        log.info("Creating BPMN Model");

        bpmnServer.createBPMNModel(activeFilters);

        return null;
    }

    @Override
    public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response,
                                         Object object, Exception exc) {

        ModelAndView modelAndView = new ModelAndView("decision");

        modelAndView.getModel().put("message", exc.getMessage());
        return modelAndView;

    }
}
