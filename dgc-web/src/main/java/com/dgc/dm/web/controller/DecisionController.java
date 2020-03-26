/*
  @author david
 */

package com.dgc.dm.web.controller;

import com.dgc.dm.core.db.service.DbServer;
import com.dgc.dm.core.dto.ProjectDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

@Controller
@Slf4j
public class DecisionController implements HandlerExceptionResolver {

    @Autowired
    DbServer dbServer;

    @RequestMapping(value = "decision", method = RequestMethod.POST)
    public List<String> populateFilters(ModelMap modelMap) {
        List<String> filters = new ArrayList<>();
        ProjectDto project = (ProjectDto) modelMap.getAttribute("project");
        dbServer.getFilters(project);

        return filters;
    }

    @Override
    public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response,
                                         Object object, Exception exc) {

        ModelAndView modelAndView = new ModelAndView("decision");

        modelAndView.getModel().put("message", exc.getMessage());
        return modelAndView;

    }
}
