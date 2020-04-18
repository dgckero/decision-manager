/*
  @author david
 */

package com.dgc.dm.web.controller;

import com.dgc.dm.core.dto.FilterCreationDto;
import com.dgc.dm.core.dto.FilterDto;
import com.dgc.dm.web.controller.iface.ProcessFiltersController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/filterList")
public class ProcessFiltersControllerImpl extends CommonController implements ProcessFiltersController {

    public final ModelAndView processFilters(@ModelAttribute FilterCreationDto form, @RequestParam(required = false, name = "emailTemplate") String emailTemplate) {
        log.info("Processing filters");

        ModelAndView modelAndView = new ModelAndView(RESULT_VIEW);
        List<FilterDto> filters = form.getFilters();
        if (null == filters || filters.isEmpty()) {
            log.warn("No filters found");
            modelAndView.getModel().put("message", "No filters found");
            modelAndView.setViewName(ERROR_VIEW);
        } else {
            log.info("Got filters {}", filters);
            List<FilterDto> activeFilters = getModelFacade().getActiveFilters(filters);
            if (activeFilters.isEmpty()) {
                modelAndView.getModel().put("message", "No Active filters found");
            } else {
                try {
                    List<Map<String, Object>> result = getModelFacade().createBPMNModel(activeFilters, emailTemplate);
                    if (null == result || result.isEmpty()) {
                        log.warn("No elements found that fit filters defined by user");
                        modelAndView.getModel().put("message", "No elements found that fit filters defined by user");
                        modelAndView.setViewName(ERROR_VIEW);
                    } else {
                        modelAndView.addObject("form", result);
                    }
                } catch (Exception e) {
                    modelAndView.getModel().put("message", e.getMessage());
                    modelAndView.setViewName(ERROR_VIEW);

                    log.error("Error {}", e.getMessage());
                    e.printStackTrace();
                }
            }
        }
        return modelAndView;
    }
}
