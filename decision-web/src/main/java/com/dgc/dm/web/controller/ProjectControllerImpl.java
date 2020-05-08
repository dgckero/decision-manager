/*
  @author david
 */

package com.dgc.dm.web.controller;

import com.dgc.dm.core.dto.FilterCreationDto;
import com.dgc.dm.core.dto.FilterDto;
import com.dgc.dm.core.dto.ProjectDto;
import com.dgc.dm.core.exception.DecisionException;
import com.dgc.dm.web.controller.iface.ProjectController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
public class ProjectControllerImpl extends CommonController implements ProjectController {

    /**
     * Generate Dmn file based on project.DmnFile
     *
     * @param project
     * @param response
     * @throws IOException
     */
    private static void generateDmnFile (final ProjectDto project, final HttpServletResponse response) {
        log.debug("[INIT] generateDmnFile project: {}", project);
        try {
            // create full filename and get input stream
            final File dmnFile = new File("temp/" + project.getName() + "decision-manager.dm");
            writeByte(dmnFile, project.getDmnFile());
            final InputStream is = new FileInputStream(dmnFile);
            // set file as attached data and copy file data to response output stream
            response.setHeader("Content-Disposition", "attachment; filename=\"" + dmnFile.getName() + "\"");
            FileCopyUtils.copy(is, response.getOutputStream());
            // delete file on server file system
            dmnFile.deleteOnExit();
            // close stream and return to view
            response.flushBuffer();
        } catch (final IOException e) {
            log.error("Error generating DMN File: {}", e.getMessage());
            e.printStackTrace();
            throw new DecisionException("Error generando el Fichero DMN del proyecto, por favor póngase en contacto con el administrador");
        }
        log.debug("[END] generateDmnFile file");
    }

    /**
     * Method which write array byte into a file
     *
     * @param file
     * @param bytes
     */
    private static void writeByte (final File file, final byte[] bytes) throws IOException {
        log.debug("[INIT] writeByte");
        final OutputStream os = new FileOutputStream(file);
        os.write(bytes);
        log.trace("Successfully project dmn inserted into file");
        os.close();
        log.debug("[END] writeByte");
    }

    /**
     * Go to view existing projects
     *
     * @return view existing projects
     */
    @Override
    public ModelAndView getProjects ( ) {
        log.info("[INIT] Go to view project view");
        ModelAndView modelAndView = new ModelAndView(SELECT_PROJECT_VIEW);
        Map<String, List<Map<String, Object>>> projects = getModelFacade().getExistingProjects();
        if (null == projects || projects.isEmpty()) {
            throw new DecisionException("No se han encontrado proyectos guardados");
        } else {
            log.info("Found {} projects", projects.size());
            modelAndView.addAllObjects(projects);
            modelAndView.addObject("selectedProject", ProjectDto.builder().build());
        }
        log.info("[END] Go to view project view");
        return modelAndView;
    }

    /**
     * Create project
     *
     * @param projectName
     * @param file
     * @return decision view
     */
    @Override
    public ModelAndView createProject (@RequestParam("name") final String projectName, @RequestParam("file") final MultipartFile file) throws IOException {
        log.info("[INIT] createProject file {} projectName {}", file.getOriginalFilename(), projectName);
        final ModelAndView modelAndView = new ModelAndView(CommonController.FILTERS_VIEW);

        ProjectDto project = getExcelFacade().processExcel(file, projectName);
        if (null == project) {
            log.error("Error creating project");
            throw new DecisionException("No se ha podido crear el proyecto, por favor póngase en contacto con el administrador");
        } else {
            modelAndView.getModel().put("project", project);
            this.getModelFacade().addFilterInformationToModel(modelAndView, project);
        }
        log.info("[END] createProject file {} projectName {}", file.getOriginalFilename(), projectName);
        return modelAndView;
    }

    /**
     * Add Information to existing project
     *
     * @param id   project's Id
     * @param file
     * @return decision view
     */
    @Override
    public final ModelAndView addInformationToProject (@PathVariable final Integer id, @RequestParam("file") final MultipartFile file) throws DecisionException {
        log.info("[INIT] adding information {} to existing projectId {}", file.getOriginalFilename(), id);
        final ModelAndView modelAndView = new ModelAndView(CommonController.FILTERS_VIEW);

        try {
            final ProjectDto project = this.getExcelFacade().processExcel(file, id);
            if (null == project) {
                log.error("Error adding info to projectId: {}", id);
                throw new DecisionException("No se ha podido añadir información al proyecto, por favor póngase en contacto con el administrador");
            } else {
                modelAndView.getModel().put("project", project);
                this.getModelFacade().addFilterInformationToModel(modelAndView, project);
                log.info("File successfully processed");
            }
        } catch (final DecisionException e) {
            throw e;
        } catch (final Exception e) {
            log.error("Error Uploading file {}", e.getMessage());
            e.printStackTrace();
            throw new DecisionException("No se ha podido añadir información al proyecto, por favor póngase en contacto con el administrador");
        }
        log.info("[END] file processed");
        return modelAndView;
    }

    /**
     * Go to edit project's filters view
     *
     * @param id project's Id
     * @return decision view
     */
    @Override
    public ModelAndView getProjectFilters (@PathVariable final Integer id) {
        log.info("[INIT] Go to edit filters for projectId: {}", id);
        final ModelAndView modelAndView = new ModelAndView(CommonController.FILTERS_VIEW);

        final ProjectDto project = getProjectById(id);
        modelAndView.getModel().put("project", project);
        this.getModelFacade().addFilterInformationToModel(modelAndView, project);

        log.info("[END] filters successfully updated");
        return modelAndView;
    }

    /**
     * Edit project's email template
     *
     * @param id            project's Id
     * @param emailTemplate
     * @return success view
     */
    @Override
    public final ModelAndView editEmailTemplate (@PathVariable final Integer id, @RequestBody String emailTemplate) {
        log.info("[INIT] Edit email template ({}) for projectId {}", emailTemplate, id);
        final ModelAndView modelAndView = new ModelAndView(SUCCESS_VIEW);

        final ProjectDto project = getProjectById(id);
        project.setEmailTemplate(emailTemplate);
        this.getModelFacade().updateProject(project);

        log.info("[END] Email template successfully updated for project{}", project);
        return modelAndView;
    }

    /**
     * Show project's filtered results on result view
     *
     * @param id project's Id
     * @return result view
     */
    @Override
    public final ModelAndView getFilteredResults (@PathVariable final Integer id) {
        log.info("[INIT] getting results filtering by projecId: {}", id);
        final ModelAndView modelAndView = new ModelAndView(RESULT_VIEW);
        final ProjectDto project = getProjectById(id);

        if (null == project.getDmnFile() || 0 >= project.getDmnFile().length) {
            log.error("DMN file NOT defined on project {}", project);
            modelAndView.setViewName(ERROR_VIEW);
            modelAndView.getModel().put(MODEL_MESSAGE, "No se ha definido una tabla de decisión para el Proyecto, por favor añada filtros y vuelva a intentarlo");
        } else {
            final List<Map<String, Object>> result = this.getModelFacade().executeDmn(project);
            if (null == result) {
                modelAndView.setViewName(ERROR_VIEW);
                modelAndView.getModel().put(MODEL_MESSAGE, "No se han encontrado resultados que cumplan con los filtros definidos");
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
     * @param id       project's Id
     * @param response
     * @throws IOException
     */
    @Override
    public void getProjectDmn (@PathVariable final Integer id, final HttpServletResponse response) throws IOException {
        log.info("[INIT] Getting dmn file for projectId {}", id);

        generateDmnFile(getProjectById(id), response);

        log.info("[END] Got  dmn file for projectId {}", id);
    }

    /**
     * Update project's DMN
     *
     * @param id      project's Id
     * @param dmnFile
     * @return result view
     */
    @Override
    public final ModelAndView editDmn (@PathVariable final Integer id, @RequestParam("dmnFile") final MultipartFile dmnFile) {
        log.info("[INIT] editDmn for projectId: {}", id);
        final ModelAndView modelAndView = new ModelAndView(RESULT_VIEW);
        final ProjectDto project = getProjectById(id);

        try {
            log.info("Validating DMN file {} for project {}", dmnFile.getOriginalFilename(), project);
            this.getModelFacade().validateDmn(project, dmnFile.getBytes());
            log.info("DMN File successfully processed");

            log.info("Running DMN");
            final List<Map<String, Object>> result = this.getModelFacade().executeDmn(project);
            if (null == result) {
                throw new DecisionException("No se han encontrado datos que cumplan con la tabla de decision añadida");
            } else {
                modelAndView.addObject("form", result);
                log.info("Results filtered by DMN file successfully processed");
            }
        } catch (final Exception e) {
            log.error("Error validating DMN File {}", e.getMessage());
            e.printStackTrace();
            throw new DecisionException(e.getMessage());
        }

        log.info("[END] editDmn for project: {}", project);
        return modelAndView;
    }

    /**
     * Get all project's registers
     *
     * @param id project's Id
     * @return result view
     */
    @Override
    public final ModelAndView getAllRegisters (@PathVariable final Integer id) {
        log.info("[INIT] Get all Registers for projectId {}", id);

        final ModelAndView modelAndView = new ModelAndView(RESULT_VIEW);
        final ProjectDto project = this.getProjectById(id);
        final List<Map<String, Object>> result = this.getModelFacade().getRowData(project);
        if (null == result) {
            throw new DecisionException("No se han encontrado datos para el proyecto: " + project.getName());
        } else {
            modelAndView.addObject("form", result);
            log.info("Found {} registers for project {}", result.size(), project);
        }
        log.info("[END] Get all Registers for project {}", project);
        return modelAndView;
    }

    /**
     * Delete all project's registers
     *
     * @param id project's Id
     * @return success view
     */
    @Override
    public final ModelAndView deleteRegisters (@PathVariable final Integer id) {
        log.info("[INIT] Delete all registers for projectId {}", id);

        final ModelAndView modelAndView = new ModelAndView(SUCCESS_VIEW);
        final ProjectDto project = this.getProjectById(id);

        this.getModelFacade().deleteRowData(project);
        modelAndView.addObject(MODEL_MESSAGE, "Registros borrados correctamente");

        log.info("[END] Delete all registers for project {}", project);
        return modelAndView;
    }

    /**
     * Delete project
     *
     * @param id project's Id
     * @return success view
     */
    @Override
    public final ModelAndView deleteProject (@PathVariable final Integer id) {
        log.info("[INIT] Delete projectId {}", id);

        final ModelAndView modelAndView = new ModelAndView(SUCCESS_VIEW);
        final ProjectDto project = this.getProjectById(id);

        this.getModelFacade().deleteProject(project);
        modelAndView.addObject(MODEL_MESSAGE, "Proyecto borrado correctamente");

        log.info("[END] Delete project {}", project);
        return modelAndView;
    }

    /**
     * Add filters to project
     *
     * @param form
     * @param emailTemplate
     * @param sendEmail
     * @return result view
     */
    @Override
    public final ModelAndView addFilters (@RequestParam(required = false, name = "emailTemplate") final String emailTemplate, @PathVariable final Boolean sendEmail, @ModelAttribute FilterCreationDto form) {
        log.info("[INIT] Processing filters with form {}, emailTemplate {}, sendEmail {}", form, emailTemplate, sendEmail);

        ModelAndView modelAndView = new ModelAndView(RESULT_VIEW);
        List<FilterDto> filters = form.getFilters();
        if (null == filters || filters.isEmpty()) {
            log.warn("No filters found");
            throw new DecisionException("No se han podido generar los filtros, por favor póngase en contacto con el administrador");
        } else {
            log.info("Got filters {}", filters);
            try {
                final ProjectDto project = filters.get(0).getProject();
                if (null == project) {
                    log.error("No project found ");
                    throw new DecisionException("No se han podido recuperar el proyecto de los filtros, por favor póngase en contacto con el administrador");
                } else {

                    List<Map<String, Object>> result = Collections.unmodifiableList(getModelFacade().createDMNModel(project, filters, (null != sendEmail && sendEmail ? emailTemplate : null), sendEmail));
                    if (result.isEmpty()) {
                        throw new DecisionException("No se han encontrado datos que cumplan con los filtros definidos");
                    } else {
                        modelAndView.addObject("form", result);
                        modelAndView.getModel().put("project", project);
                    }
                }
            } catch (Exception e) {
                log.error("Error processing filters: {}", e.getMessage());
                e.printStackTrace();
                throw new DecisionException("Error procesando los filtros: " + e.getMessage());
            }
        }
        log.info("[END] Processing filters with form {}, emailTemplate {}, sendEmail {}", form, emailTemplate, sendEmail);
        return modelAndView;
    }

    /**
     * Go to view project based on selectedProjectId
     *
     * @param id project's Id
     * @return view project
     */
    @Override
    public ModelAndView getProject (@RequestParam("id") Integer id) {
        log.info("[INIT] viewProject by id: {}", id);
        ModelAndView modelAndView = new ModelAndView(EDIT_PROJECT);
        if (id == null) {
            log.error("SelectedProjectId is NULL");
            throw new DecisionException("No se ha enviado el identificador del proyecto en la consulta");
        } else {
            log.info("SelectedProjectId " + id);
            ProjectDto project = this.getProjectById(id);
            log.info("Got project " + project);
            modelAndView.addObject("selectedProject", project);
        }
        log.info("[END] viewProject by id: {}", id);
        return modelAndView;
    }

    /**
     * Get project by Id
     *
     * @param id
     * @return projectDto
     * @throws DecisionException if project is not found
     */
    private ProjectDto getProjectById (Integer id) throws DecisionException {
        log.debug("[INIT] getProjectById id: {}", id);
        final ProjectDto project = this.getModelFacade().getProject(id);
        if (null == project) {
            log.error("[END] No project found by Id {}", id);
            throw new DecisionException("No se ha encontrado el proyecto con id: " + id);
        }
        log.debug("[END] getProjectById found: {}", project);
        return project;
    }
}
