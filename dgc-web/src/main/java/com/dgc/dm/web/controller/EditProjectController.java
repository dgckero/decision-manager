/*
  @author david
 */

package com.dgc.dm.web.controller;

import com.dgc.dm.core.db.service.DbServer;
import com.dgc.dm.core.dto.ProjectDto;
import com.dgc.dm.core.service.bpmn.BPMNServer;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.model.dmn.DmnModelException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
public class EditProjectController implements HandlerExceptionResolver {

    @Autowired
    private DbServer dbServer;
    @Autowired
    private BPMNServer bpmnServer;

    @RequestMapping(value = "/editProject", method = RequestMethod.POST, params = "action=uploadFile")
    public static final String uploadFile(@ModelAttribute("selectedProject") final ProjectDto project, @RequestParam("uploadFile") MultipartFile uploadFile) {
        log.info("processing file {} for project {}", uploadFile.getOriginalFilename(), project);

        log.info("File successfully processed");
        return "success";
    }

    @RequestMapping(value = "/editProject", method = RequestMethod.POST, params = "action=editFilters")
    public static final String editFilters(@ModelAttribute("selectedProject") final ProjectDto project) {
        log.info("Go to edit filters for project {}", project);

        ModelAndView modelAndView = new ModelAndView("decision");
        modelAndView.getModel().put("project", project);

        log.info("File successfully processed");
        return "success";
    }

    private static File generateDmnFile(final ProjectDto project) throws IOException {
        File dmnFile = new File(project.getName() + "decision-manager.dm");
        OutputStream os = new FileOutputStream(dmnFile);
        os.write(project.getDmnFile());
        os.close();

        return dmnFile;
    }

    @RequestMapping(value = "/editProject", method = RequestMethod.POST, params = "action=editEmailTemplate")
    public final String editEmailTemplate(@ModelAttribute("selectedProject") final ProjectDto selectedProject, @ModelAttribute("emailTemplate") String emailTemplate) {
        log.info("Edit email template ({}) for project {}", emailTemplate, selectedProject);

        selectedProject.setEmailTemplate(emailTemplate);
        this.dbServer.updateProject(selectedProject);

        log.info("Email template successfully updated for project{}", selectedProject);
        return "success";
    }

    @RequestMapping(value = "/editProject", method = RequestMethod.POST, params = "action=getDMNFilteredResults")
    public final ModelAndView getDMNFilteredResults(@ModelAttribute("selectedProject") final ProjectDto selectedProject) {
        log.info("getting results filtering by DMN file");
        final ModelAndView modelAndView = new ModelAndView("result");

        log.debug("Getting updated project from database");
        ProjectDto updatedProject = dbServer.getProject(selectedProject.getId());

        if (null == updatedProject.getDmnFile() || 0 >= updatedProject.getDmnFile().length) {
            log.error("DMN file NOT defined on project {}", updatedProject);
            modelAndView.setViewName("error");
            modelAndView.getModel().put("message", "DMN file NOT defined, please define a DMN file on view Project view");
        } else {
            final List<Map<String, Object>> result = bpmnServer.executeDmn(updatedProject);
            modelAndView.addObject("form", result);

            log.info("Results filtered by DMN file successfully processed");
        }
        return modelAndView;
    }

    @RequestMapping(value = "/editProject", method = RequestMethod.POST, params = "action=getDmn")
    @ResponseBody
    public final FileSystemResource getDmn(@ModelAttribute("selectedProject") final ProjectDto selectedProject) throws IOException {
        log.info("Getting dmn file for project {}", selectedProject);

        final ProjectDto projectDto = this.dbServer.getProject(selectedProject.getId());
        log.info("DMN file recovered successfully for project {}", projectDto);

        return new FileSystemResource(generateDmnFile(projectDto));
    }

    @RequestMapping(value = "/editProject", method = RequestMethod.POST, params = "action=editDmn")
    public final ModelAndView editDmn(@ModelAttribute("selectedProject") final ProjectDto project, @RequestParam("dmnFile") MultipartFile dmnFile) {
        ModelAndView modelAndView = new ModelAndView("success");

        try {
            log.info("Validating DMN file {} for project {}", dmnFile.getOriginalFilename(), project);
            bpmnServer.validateDmn(dmnFile.getBytes());
            project.setDmnFile(dmnFile.getBytes());
            dbServer.updateProject(project);
            log.info("DMN File successfully processed");
        } catch (DmnModelException e) {
            e.printStackTrace();
        } catch (Exception e) {
            log.error("Error validating DMN File {}", e.getMessage());
            e.printStackTrace();
            modelAndView.setViewName("error");
            modelAndView.addObject("message", "Project NOT found");
        }

        return modelAndView;
    }

    @RequestMapping(value = "/editProject", method = RequestMethod.POST, params = "action=getAllRegisters")
    public final ModelAndView getAllRegisters(@ModelAttribute("selectedProject") final ProjectDto project) {
        log.info("Get all Registers for project {}", project);

        final ModelAndView modelAndView = new ModelAndView("result");
        final List<Map<String, Object>> result = dbServer.getCommonData(project);
        modelAndView.addObject("form", result);

        log.info("Found {} registers for project {}", result.size(), project);
        return modelAndView;
    }

    @RequestMapping(value = "/editProject", method = RequestMethod.POST, params = "action=deleteRegisters")
    public final ModelAndView deleteRegisters(@ModelAttribute("selectedProject") final ProjectDto project) {
        log.info("Delete all registers for project {}", project);

        final ModelAndView modelAndView = new ModelAndView("success");
        this.dbServer.deleteCommonData(project);
        modelAndView.addObject("message", "Registros borrados correctamente");

        log.info("Deleted all registers for project {}", project);

        return modelAndView;
    }

    @RequestMapping(value = "/editProject", method = RequestMethod.POST, params = "action=deleteProject")
    public final ModelAndView deleteProject(@ModelAttribute("selectedProject") final ProjectDto project) {
        log.info("Delete project {}", project);

        final ModelAndView modelAndView = new ModelAndView("success");
        this.dbServer.deleteProject(project);
        modelAndView.addObject("message", "Proyecto borrado correctamente");

        log.info("Deleted project {}", project);

        return modelAndView;
    }

    @Override
    public final ModelAndView resolveException(final HttpServletRequest request, final HttpServletResponse response,
                                               final Object object, final Exception exc) {

        final ModelAndView modelAndView = new ModelAndView("home");
        modelAndView.getModel().put("message", exc.getMessage());

        log.error("Error {}", exc.getMessage());
        exc.printStackTrace();

        return modelAndView;

    }
}
