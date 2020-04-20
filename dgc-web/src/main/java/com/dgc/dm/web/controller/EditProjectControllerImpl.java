/*
  @author david
 */

package com.dgc.dm.web.controller;

import com.dgc.dm.core.dto.ProjectDto;
import com.dgc.dm.web.controller.iface.EditProjectController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
public class EditProjectControllerImpl extends CommonController implements EditProjectController {

    private static File generateDmnFile(ProjectDto project) throws IOException {
        final File dmnFile = new File(project.getName() + "decision-manager.dm");
        final OutputStream os = new FileOutputStream(dmnFile);
        os.write(project.getDmnFile());
        os.close();

        return dmnFile;
    }

    @Override
    public String uploadFile(@ModelAttribute("selectedProject") final ProjectDto project, @RequestParam("uploadFile") final MultipartFile uploadFile) {
        log.info("processing file {} for project {}", uploadFile.getOriginalFilename(), project);
//TODO add new data to project
        log.info("File successfully processed");
        return CommonController.SUCCESS_VIEW;
    }

    @Override
    public ModelAndView editFilters(@ModelAttribute("selectedProject") final ProjectDto project) {
        log.info("Go to edit filters for project {}", project);
        final ModelAndView modelAndView = new ModelAndView(CommonController.DECISION_VIEW);
        if (null == project) {
            log.error("Error creating project");
            modelAndView.setViewName(CommonController.ERROR_VIEW);
            modelAndView.getModel().put("message", "Error creating project");
        } else {
            modelAndView.getModel().put("project", project);
            this.getModelFacade().addFilterInformationToModel(modelAndView, project);
        }
        log.info("File successfully processed");
        return modelAndView;
    }

    @Override
    public final String editEmailTemplate(@ModelAttribute("selectedProject") ProjectDto selectedProject, @ModelAttribute("emailTemplate") final String emailTemplate) {
        log.info("Edit email template ({}) for project {}", emailTemplate, selectedProject);

        selectedProject.setEmailTemplate(emailTemplate);
        this.getModelFacade().updateProject(selectedProject);

        log.info("Email template successfully updated for project{}", selectedProject);
        return "success";
    }

    @Override
    public final ModelAndView getDMNFilteredResults(@ModelAttribute("selectedProject") ProjectDto selectedProject) {
        log.info("getting results filtering by DMN file");
        ModelAndView modelAndView = new ModelAndView(RESULT_VIEW);

        log.debug("Getting updated project from database");
        final ProjectDto updatedProject = this.getModelFacade().getProject(selectedProject.getId());

        if (null == updatedProject.getDmnFile() || 0 >= updatedProject.getDmnFile().length) {
            log.error("DMN file NOT defined on project {}", updatedProject);
            modelAndView.setViewName(ERROR_VIEW);
            modelAndView.getModel().put("message", "DMN file NOT defined, please define a DMN file on view Project view");
        } else {
            List<Map<String, Object>> result = this.getModelFacade().executeDmn(updatedProject);
            if (null == result) {
                modelAndView.setViewName(ERROR_VIEW);
                modelAndView.getModel().put("message", "\"No results found after running DMN for project" + updatedProject.getName());
            } else {
                modelAndView.addObject("form", result);
                log.info("Results filtered by DMN file successfully processed");
            }
        }
        return modelAndView;
    }

    @Override
    public final FileSystemResource getDmn(@ModelAttribute("selectedProject") ProjectDto selectedProject) throws IOException {
        log.info("Getting dmn file for project {}", selectedProject);

        ProjectDto projectDto = this.getModelFacade().getProject(selectedProject.getId());
        log.info("DMN file recovered successfully for project {}", projectDto);

        return new FileSystemResource(generateDmnFile(projectDto));
    }

    @Override
    public final ModelAndView editDmn(@ModelAttribute("selectedProject") ProjectDto project, @RequestParam("dmnFile") final MultipartFile dmnFile) {
        final ModelAndView modelAndView = new ModelAndView(SUCCESS_VIEW);
        try {
            log.info("Validating DMN file {} for project {}", dmnFile.getOriginalFilename(), project);
            this.getModelFacade().validateDmn(project, dmnFile.getBytes());
            log.info("DMN File successfully processed");
        } catch (final Exception e) {
            log.error("Error validating DMN File {}", e.getMessage());
            e.printStackTrace();
            modelAndView.setViewName(ERROR_VIEW);
            modelAndView.addObject("message", "Project NOT found");
        }
        return modelAndView;
    }

    @Override
    public final ModelAndView getAllRegisters(@ModelAttribute("selectedProject") ProjectDto project) {
        log.info("Get all Registers for project {}", project);

        ModelAndView modelAndView = new ModelAndView(RESULT_VIEW);
        if (null == project) {
            log.error("Selected project is null");
            modelAndView.setViewName(ERROR_VIEW);
            modelAndView.getModel().put("message", "Selected project is null");
        } else {
            List<Map<String, Object>> result = this.getModelFacade().getCommonData(project);
            if (null == result) {
                log.error("Not found data from project {}", project);
                modelAndView.setViewName(ERROR_VIEW);
                modelAndView.getModel().put("message", "Not found data from project" + project.getName());
            } else {
                modelAndView.addObject("form", result);
                log.info("Found {} registers for project {}", result.size(), project);
            }
        }
        return modelAndView;
    }

    @Override
    public final ModelAndView deleteRegisters(@ModelAttribute("selectedProject") ProjectDto project) {
        log.info("Delete all registers for project {}", project);

        ModelAndView modelAndView = new ModelAndView(SUCCESS_VIEW);
        if (null == project) {
            log.error("Selected project is null");
            modelAndView.setViewName(ERROR_VIEW);
            modelAndView.getModel().put("message", "Selected project is null");
        } else {
            this.getModelFacade().deleteCommonData(project);
            modelAndView.addObject("message", "Registros borrados correctamente");

            log.info("Deleted all registers for project {}", project);
        }
        return modelAndView;
    }

    @Override
    public final ModelAndView deleteProject(@ModelAttribute("selectedProject") ProjectDto project) {
        log.info("Delete project {}", project);

        ModelAndView modelAndView = new ModelAndView(SUCCESS_VIEW);
        if (null == project) {
            log.error("Selected project is null");
            modelAndView.setViewName(ERROR_VIEW);
            modelAndView.getModel().put("message", "Selected project is null");
        } else {
            this.getModelFacade().deleteProject(project);
            modelAndView.addObject("message", "Proyecto borrado correctamente");
            log.info("Deleted project {}", project);
        }
        return modelAndView;
    }

}
