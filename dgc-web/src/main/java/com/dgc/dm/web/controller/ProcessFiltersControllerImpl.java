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

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/filterList")
public class ProcessFiltersControllerImpl extends CommonController implements ProcessFiltersController {

    public final ModelAndView processFilters(@ModelAttribute FilterCreationDto form, @RequestParam(required = false, name = "emailTemplate") String emailTemplate,
                                             @RequestParam(required = false, name = "sendEmail") Boolean sendEmail) {
        log.info("Processing filters with form {}, emailTemplate {}, sendEmail {}", form, emailTemplate, sendEmail);

        ModelAndView modelAndView = new ModelAndView(RESULT_VIEW);
        List<FilterDto> filters = form.getFilters();
        if (null == filters || filters.isEmpty()) {
            log.warn("No filters found");
            modelAndView.getModel().put("message", "No filters found");
            modelAndView.setViewName(ERROR_VIEW);
        } else {
            log.info("Got filters {}", filters);
            if (filters.isEmpty()) {
                modelAndView.getModel().put("message", "No Filters found");
            } else {
                try {
                    List<Map<String, Object>> result = Collections.unmodifiableList(getModelFacade().createBPMNModel(filters, (null != sendEmail && sendEmail ? emailTemplate : null), sendEmail));
                    if (result.isEmpty()) {
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
