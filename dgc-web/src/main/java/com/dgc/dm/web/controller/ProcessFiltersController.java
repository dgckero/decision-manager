/**
 * @author david
 */

package com.dgc.dm.web.controller;

import com.dgc.dm.core.dto.FilterCreationDto;
import com.dgc.dm.core.dto.FilterDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Slf4j
@Controller
@RequestMapping("/filterList")
public class ProcessFiltersController {

    @PostMapping("/process")
    public String processFilters(@ModelAttribute FilterCreationDto form, Model model) {

        List<FilterDto> filters = form.getFilters();

        log.info("Got filters " + filters);

        return null;
    }
}
