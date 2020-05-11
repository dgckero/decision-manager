/*
  @author david
 */

package com.dgc.dm.web.controller.iface;

import com.dgc.dm.core.dto.FilterCreationDto;
import com.dgc.dm.core.exception.DecisionException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RequestMapping("/projects")
public interface ProjectController {
    @RequestMapping(value = "/get/all", method = RequestMethod.POST)
    ModelAndView getProjects ( ) throws DecisionException;

    @RequestMapping(value = "/create", method = RequestMethod.POST)
    ModelAndView createProject (@RequestParam("name") String projectName, @RequestParam("file") MultipartFile file) throws IOException;

    @RequestMapping(value = "/edit/{id}/results", method = RequestMethod.POST)
    ModelAndView addInformationToProject (@PathVariable Integer id, @RequestParam("file") MultipartFile file) throws DecisionException;

    @RequestMapping(value = "/get/{id}/filters", method = RequestMethod.POST)
    ModelAndView getProjectFilters (@PathVariable Integer id);

    @RequestMapping(value = "/edit/{id}/emailTemplate", method = RequestMethod.POST)
    ModelAndView editEmailTemplate (@PathVariable Integer id, @RequestParam("emailTemplate") String emailTemplate);

    @RequestMapping(value = "/get/{id}/results", method = RequestMethod.POST, params = "filtered=true")
    ModelAndView getFilteredResults (@PathVariable Integer id);

    @RequestMapping(value = "/get/{id}/dmnFile", method = RequestMethod.POST)
    @ResponseBody
    void getProjectDmn (@PathVariable Integer id, HttpServletResponse response);

    @RequestMapping(value = "/edit/{id}/dmnFile", method = RequestMethod.POST)
    ModelAndView editDmn (@PathVariable Integer id, @RequestParam("dmnFile") MultipartFile dmnFile);

    @RequestMapping(value = "/get/{id}/results", method = RequestMethod.POST, params = "filtered=false")
    ModelAndView getAllRegisters (@PathVariable Integer id);

    @RequestMapping(value = "/delete/{id}/results", method = RequestMethod.POST)
    ModelAndView deleteRegisters (@PathVariable Integer id);

    @RequestMapping(value = "/delete/{id}", method = RequestMethod.POST)
    ModelAndView deleteProject (@PathVariable Integer id);

    @RequestMapping(value = "/edit/filters", method = RequestMethod.POST)
    ModelAndView addFilters (@RequestParam(required = false, name = "emailTemplate") String emailTemplate, @RequestParam(required = false, name = "sendEmail") Boolean sendEmail, @ModelAttribute FilterCreationDto form, HttpServletRequest request);

    @RequestMapping(value = "/get", method = RequestMethod.POST)
    ModelAndView getProject (@RequestParam("id") Integer id);

}
