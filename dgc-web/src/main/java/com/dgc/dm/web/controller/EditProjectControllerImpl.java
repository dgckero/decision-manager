/*
  @author david
 */

package com.dgc.dm.web.controller;

import com.dgc.dm.core.dto.ProjectDto;
import com.dgc.dm.web.controller.iface.EditProjectController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
public class EditProjectControllerImpl extends CommonController implements EditProjectController {

    /**
     * Generate Dmn file based on project.DmnFile
     *
     * @param project
     * @param response
     * @throws IOException
     */
    private static void generateDmnFile(ProjectDto project, HttpServletResponse response) throws IOException {
        log.debug("[INIT] generateDmnFile project: {}", project);
        // create full filename and get input stream
        File dmnFile = new File("temp/" + project.getName() + "decision-manager.dm");
        writeByte(dmnFile, project.getDmnFile());
        InputStream is = new FileInputStream(dmnFile);

        // set file as attached data and copy file data to response output stream
        response.setHeader("Content-Disposition", "attachment; filename=\"" + dmnFile.getName() + "\"");
        FileCopyUtils.copy(is, response.getOutputStream());

        // delete file on server file system
        dmnFile.deleteOnExit();
        // close stream and return to view
        response.flushBuffer();
        log.debug("[END] generateDmnFile file");
    }

    /**
     * Method which write array byte into a file
     *
     * @param file
     * @param bytes
     */
    private static void writeByte(File file, byte[] bytes) {
        try {
            log.debug("[INIT] writeByte");
            OutputStream os = new FileOutputStream(file);
            os.write(bytes);
            log.trace("Successfully project dmn inserted into file");
            os.close();
        } catch (Exception e) {
            log.error("Error " + e.getMessage());
        }
        log.debug("[END] writeByte");
    }

    /**
     * Process Excel file, updates project model and get columns defined on it
     *
     * @param project
     * @param uploadFile
     * @return decision view
     */
    @Override
    public final ModelAndView uploadFile(@ModelAttribute("selectedProject") ProjectDto project, @RequestParam("uploadFile") MultipartFile uploadFile) {
        log.info("[INIT] processing file {} for {}", uploadFile.getOriginalFilename(), project);
        ModelAndView modelAndView = new ModelAndView(CommonController.DECISION_VIEW);

        try {
            getExcelFacade().processExcel(uploadFile, project);
            modelAndView.getModel().put("project", project);
            getModelFacade().addFilterInformationToModel(modelAndView, project);
            log.info("File successfully processed");

        } catch (Exception e) {
            log.error("Error Uploading file {}", e.getMessage());
            modelAndView.setViewName(CommonController.ERROR_VIEW);
            modelAndView.getModel().put("message", e.getMessage());
            e.printStackTrace();
        }
        log.info("[END] file processed");
        return modelAndView;
    }

    /**
     * Go to edit project's filters view
     *
     * @param project
     * @return decision view
     */
    @Override
    public final ModelAndView editFilters(@ModelAttribute("selectedProject") ProjectDto project) {
        log.info("[INIT] Go to edit filters for project {}", project);
        ModelAndView modelAndView = new ModelAndView(CommonController.DECISION_VIEW);
        if (null == project) {
            log.error("Error creating project");
            modelAndView.setViewName(CommonController.ERROR_VIEW);
            modelAndView.getModel().put("message", "Error creating project");
        } else {
            modelAndView.getModel().put("project", project);
            getModelFacade().addFilterInformationToModel(modelAndView, project);
        }
        log.info("[END] filters successfully updated");
        return modelAndView;
    }

    /**
     * Edit project's email template
     *
     * @param selectedProject
     * @param emailTemplate
     * @return
     */
    @Override
    public final ModelAndView editEmailTemplate(@ModelAttribute("selectedProject") ProjectDto selectedProject, @ModelAttribute("emailTemplate") String emailTemplate) {
        log.info("[INIT] Edit email template ({}) for project {}", emailTemplate, selectedProject);

        ModelAndView modelAndView = new ModelAndView(SUCCESS_VIEW);
        selectedProject.setEmailTemplate(emailTemplate);
        getModelFacade().updateProject(selectedProject);

        log.info("[END] Email template successfully updated for project{}", selectedProject);
        return modelAndView;
    }

    /**
     * Show project's filtered results on result view
     *
     * @param selectedProject
     * @return result view
     */
    @Override
    public final ModelAndView getDMNFilteredResults(@ModelAttribute("selectedProject") ProjectDto selectedProject) {
        log.info("[INIT] getting results filtering by DMN file");
        ModelAndView modelAndView = new ModelAndView(RESULT_VIEW);

        log.debug("Getting updated project from database");
        ProjectDto updatedProject = getModelFacade().getProject(selectedProject.getId());

        if (null == updatedProject.getDmnFile() || 0 >= updatedProject.getDmnFile().length) {
            log.error("DMN file NOT defined on project {}", updatedProject);
            modelAndView.setViewName(ERROR_VIEW);
            modelAndView.getModel().put("message", "DMN file NOT defined, please define a DMN file on view Project view");
        } else {
            List<Map<String, Object>> result = getModelFacade().executeDmn(updatedProject);
            if (null == result) {
                modelAndView.setViewName(ERROR_VIEW);
                modelAndView.getModel().put("message", "\"No results found after running DMN for project" + updatedProject.getName());
            } else {
                modelAndView.addObject("form", result);
                log.info("Results filtered by DMN file successfully processed");
            }
        }
        log.info("[END] getting results filtering by DMN file");
        return modelAndView;
    }

    /**
     * Get DMN file
     *
     * @param selectedProject
     * @param response
     * @throws IOException
     */
    @Override
    public void getDmn(@ModelAttribute("selectedProject") ProjectDto selectedProject, HttpServletResponse response) throws IOException {
        log.info("[INIT] Getting dmn file for project {}", selectedProject);

        ProjectDto projectDto = getModelFacade().getProject(selectedProject.getId());
        log.info("DMN file recovered successfully for project {}", projectDto);
        generateDmnFile(projectDto, response);

        log.info("[END] Got  dmn file for project {}", selectedProject);
    }

    /**
     * Update project's DMN
     *
     * @param project
     * @param dmnFile
     * @return result view
     */
    @Override
    public final ModelAndView editDmn(@ModelAttribute("selectedProject") ProjectDto project, @RequestParam("dmnFile") MultipartFile dmnFile) {
        log.info("[INIT] editDmn for project: {}", project);
        ModelAndView modelAndView = new ModelAndView(RESULT_VIEW);
        try {
            log.info("Validating DMN file {} for project {}", dmnFile.getOriginalFilename(), project);
            getModelFacade().validateDmn(project, dmnFile.getBytes());
            log.info("DMN File successfully processed");

            log.info("Running DMN");
            List<Map<String, Object>> result = getModelFacade().executeDmn(project);
            if (null == result) {
                modelAndView.setViewName(ERROR_VIEW);
                modelAndView.getModel().put("message", "No results found after running DMN for project" + project.getName());
            } else {
                modelAndView.addObject("form", result);
                log.info("Results filtered by DMN file successfully processed");
            }
        } catch (final IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            log.error("Error validating DMN File {}", e.getMessage());
            e.printStackTrace();
            modelAndView.setViewName(ERROR_VIEW);
            modelAndView.addObject("message", e.getMessage());
        }
        log.info("[END] editDmn for project: {}", project);
        return modelAndView;
    }

    /**
     * Get all project's registers
     *
     * @param project
     * @return result view
     */
    @Override
    public final ModelAndView getAllRegisters(@ModelAttribute("selectedProject") ProjectDto project) {
        log.info("[INIT] Get all Registers for project {}", project);

        ModelAndView modelAndView = new ModelAndView(RESULT_VIEW);
        if (null == project) {
            log.error("Selected project is null");
            modelAndView.setViewName(ERROR_VIEW);
            modelAndView.getModel().put("message", "Selected project is null");
        } else {
            List<Map<String, Object>> result = getModelFacade().getRowData(project);
            if (null == result) {
                log.error("Not found data from project {}", project);
                modelAndView.setViewName(ERROR_VIEW);
                modelAndView.getModel().put("message", "Not found data from project" + project.getName());
            } else {
                modelAndView.addObject("form", result);
                log.info("Found {} registers for project {}", result.size(), project);
            }
        }
        log.info("[END] Get all Registers for project {}", project);
        return modelAndView;
    }

    /**
     * Delete all project's registers
     *
     * @param project
     * @return success view
     */
    @Override
    public final ModelAndView deleteRegisters(@ModelAttribute("selectedProject") ProjectDto project) {
        log.info("[INIT] Delete all registers for project {}", project);

        ModelAndView modelAndView = new ModelAndView(SUCCESS_VIEW);
        if (null == project) {
            log.error("Selected project is null");
            modelAndView.setViewName(ERROR_VIEW);
            modelAndView.getModel().put("message", "Selected project is null");
        } else {
            getModelFacade().deleteRowData(project);
            modelAndView.addObject("message", "Registros borrados correctamente");

            log.info("Deleted all registers for project {}", project);
        }
        log.info("[END] Delete all registers for project {}", project);
        return modelAndView;
    }

    /**
     * Delete project
     *
     * @param project
     * @return success view
     */
    @Override
    public final ModelAndView deleteProject(@ModelAttribute("selectedProject") ProjectDto project) {
        log.info("[INIT] Delete project {}", project);

        ModelAndView modelAndView = new ModelAndView(SUCCESS_VIEW);
        if (null == project) {
            log.error("Selected project is null");
            modelAndView.setViewName(ERROR_VIEW);
            modelAndView.getModel().put("message", "Selected project is null");
        } else {
            getModelFacade().deleteProject(project);
            modelAndView.addObject("message", "Proyecto borrado correctamente");
            log.info("Deleted project {}", project);
        }
        log.info("[END] Delete project {}", project);
        return modelAndView;
    }

}
