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

    /**
     * process filters sent by user
     *
     * @param form
     * @param emailTemplate
     * @param sendEmail
     * @return result view
     */
    public final ModelAndView processFilters(@ModelAttribute final FilterCreationDto form, @RequestParam(required = false, name = "emailTemplate") final String emailTemplate,
                                             @RequestParam(required = false, name = "sendEmail") final Boolean sendEmail) {
        log.info("[INIT] Processing filters with form {}, emailTemplate {}, sendEmail {}", form, emailTemplate, sendEmail);

        final ModelAndView modelAndView = new ModelAndView(RESULT_VIEW);
        final List<FilterDto> filters = form.getFilters();
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
                    final List<Map<String, Object>> result = Collections.unmodifiableList(this.getModelFacade().createDMNModel(filters, (null != sendEmail && sendEmail ? emailTemplate : null), sendEmail));
                    if (result.isEmpty()) {
                        log.warn("No elements found that fit filters defined by user");
                        modelAndView.getModel().put("message", "No elements found that fit filters defined by user");
                        modelAndView.setViewName(ERROR_VIEW);
                    } else {
                        modelAndView.addObject("form", result);
                    }
                } catch (final Exception e) {
                    modelAndView.getModel().put("message", e.getMessage());
                    modelAndView.setViewName(ERROR_VIEW);

                    log.error("Error {}", e.getMessage());
                    e.printStackTrace();
                }
            }
        }
        log.info("[END] Processing filters with form {}, emailTemplate {}, sendEmail {}", form, emailTemplate, sendEmail);
        return modelAndView;
    }
}
