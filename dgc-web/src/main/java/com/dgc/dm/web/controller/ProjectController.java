/*
  @author david
 */

package com.dgc.dm.web.controller;

import com.dgc.dm.core.db.service.DbServer;
import com.dgc.dm.core.dto.ProjectDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
public class ProjectController implements HandlerExceptionResolver {

    @Autowired
    DbServer dbServer;

    @RequestMapping(value = "/project", method = RequestMethod.POST, params = "action=newProject")
    public String newProject() {
        log.info("Go to new project view");

        return "newProject";
    }

    @RequestMapping(value = "/project", method = RequestMethod.POST, params = "action=selectProject")
    public ModelAndView selectProject() {
        log.info("Go to view project view");
        ModelAndView modelAndView = new ModelAndView("selectProject");
        modelAndView.addAllObjects(getExistingProjects());
        modelAndView.addObject("selectedProject", ProjectDto.builder().build());

        return modelAndView;
    }

    @Override
    public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response,
                                         Object object, Exception exc) {

        ModelAndView modelAndView = new ModelAndView("home");
        modelAndView.getModel().put("message", exc.getMessage());

        log.error("Error " + exc.getMessage());
        exc.printStackTrace();

        return modelAndView;

    }

    private Map<String, List<Map<String, Object>>> getExistingProjects() {
        List<Map<String, Object>> projects = dbServer.getProjects();

        if (projects == null) {
            log.warn("No projects founds");
            return null;
        } else {
            Map<String, List<Map<String, Object>>> modelMap = new HashMap<>();
            modelMap.put("existingProjects", projects);
            return modelMap;
        }
    }
}
