/*
  @author david
 */

package com.dgc.dm.web.controller.iface;

import com.dgc.dm.core.dto.FilterCreationDto;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;


public interface ProcessFiltersController {
    @PostMapping("/process")
    ModelAndView processFilters(@ModelAttribute FilterCreationDto form, @RequestParam(required = false, name = "emailTemplate") String emailTemplate,
                                @RequestParam(required = false, name = "sendEmail") Boolean sendEmail);
}
