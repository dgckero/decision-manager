/*
  @author david
 */

package com.dgc.dm.web.controller.iface;

import com.dgc.dm.core.dto.ProjectDto;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public interface EditProjectController extends HandlerExceptionResolver {
    @RequestMapping(value = "/editProject", method = RequestMethod.POST, params = "action=uploadFile")
    ModelAndView uploadFile(@ModelAttribute("selectedProject") ProjectDto project, @RequestParam("uploadFile") MultipartFile file);

    @RequestMapping(value = "/editProject", method = RequestMethod.POST, params = "action=editFilters")
    ModelAndView editFilters(@ModelAttribute("selectedProject") ProjectDto project);

    @RequestMapping(value = "/editProject", method = RequestMethod.POST, params = "action=editEmailTemplate")
    ModelAndView editEmailTemplate(@ModelAttribute("selectedProject") ProjectDto selectedProject, @ModelAttribute("emailTemplate") String emailTemplate);

    @RequestMapping(value = "/editProject", method = RequestMethod.POST, params = "action=getDMNFilteredResults")
    ModelAndView getDMNFilteredResults(@ModelAttribute("selectedProject") ProjectDto selectedProject);

    @RequestMapping(value = "/editProject", method = RequestMethod.POST, params = "action=getDmn")
    @ResponseBody
    void getDmn(@ModelAttribute("selectedProject") ProjectDto selectedProject, HttpServletResponse response) throws IOException;

    @RequestMapping(value = "/editProject", method = RequestMethod.POST, params = "action=editDmn")
    ModelAndView editDmn(@ModelAttribute("selectedProject") ProjectDto project, @RequestParam("dmnFile") MultipartFile dmnFile);

    @RequestMapping(value = "/editProject", method = RequestMethod.POST, params = "action=getAllRegisters")
    ModelAndView getAllRegisters(@ModelAttribute("selectedProject") ProjectDto project);

    @RequestMapping(value = "/editProject", method = RequestMethod.POST, params = "action=deleteRegisters")
    ModelAndView deleteRegisters(@ModelAttribute("selectedProject") ProjectDto project);

    @RequestMapping(value = "/editProject", method = RequestMethod.POST, params = "action=deleteProject")
    ModelAndView deleteProject(@ModelAttribute("selectedProject") ProjectDto project);
}
