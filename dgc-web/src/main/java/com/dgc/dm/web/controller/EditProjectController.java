/*
  @author david
 */

package com.dgc.dm.web.controller;

import com.dgc.dm.core.db.service.DbServer;
import com.dgc.dm.core.dto.ProjectDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
@Controller
public class EditProjectController implements HandlerExceptionResolver {

    @Autowired
    private DbServer dbServer;

    @RequestMapping(value = "/editProject", method = RequestMethod.POST, params = "action=editEmailTemplate")
    public String editEmailTemplate(@ModelAttribute("selectedProject") ProjectDto project, @ModelAttribute("emailTemplate") final String emailTemplate) {
        log.info("Edit email template (" + emailTemplate + ") for project " + project);

        project.setEmailTemplate(emailTemplate);
        dbServer.updateProject(project);

        log.info("Email template successfully updated for project" + project);
        return "success";
    }

    @RequestMapping(value = "/editProject", method = RequestMethod.POST, params = "action=uploadFile")
    public String uploadFile(@ModelAttribute("selectedProject") ProjectDto project, @RequestParam("uploadFile") final MultipartFile uploadFile) {
        log.info("processing file " + uploadFile.getOriginalFilename() + " for project " + project);

        log.info("File successfully processed");
        return "success";
    }

    @RequestMapping(value = "/editProject", method = RequestMethod.POST, params = "action=getDmn")
    public String getDmn(@ModelAttribute("selectedProject") ProjectDto project) {
        log.info("Getting dmn file for project " + project);

        log.info("DMN file recovered successfully");
        return "success";
    }

    @RequestMapping(value = "/editProject", method = RequestMethod.POST, params = "action=editDmn")
    public String editDmn(@ModelAttribute("selectedProject") ProjectDto project) {
        log.info("Go to Edit DMN file for project " + project);

        return "editDmn";
    }

    @RequestMapping(value = "/editProject", method = RequestMethod.POST, params = "action=getFilteredResults")
    public String getFilteredResults(@ModelAttribute("selectedProject") ProjectDto project) {
        log.info("Getting filtered results for project " + project);

        return "result";
    }

    @RequestMapping(value = "/editProject", method = RequestMethod.POST, params = "action=getAllRegisters")
    public String getAllRegisters(@ModelAttribute("selectedProject") ProjectDto project) {
        log.info("Get all Registers for project " + project);

        return "result";
    }

    @RequestMapping(value = "/editProject", method = RequestMethod.POST, params = "action=deleteRegisters")
    public String deleteRegisters(@ModelAttribute("selectedProject") ProjectDto project) {
        log.info("Delete all registers for project " + project);

        return "success";
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
}
