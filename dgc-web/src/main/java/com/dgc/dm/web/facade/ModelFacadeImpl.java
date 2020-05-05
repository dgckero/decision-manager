/*
  @author david
 */

package com.dgc.dm.web.facade;

import com.dgc.dm.core.dto.FilterCreationDto;
import com.dgc.dm.core.dto.FilterDto;
import com.dgc.dm.core.dto.ProjectDto;
import com.dgc.dm.core.service.bpmn.BPMNServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.ModelAndView;

import java.util.*;
import java.util.stream.Collectors;


@Slf4j
@Service
@Transactional
class ModelFacadeImpl extends CommonFacade implements ModelFacade {

    private static final Integer CONTACT_FILTER = 1;
    @Autowired
    private BPMNServer bpmnServer;

    /**
     * Get list of FilterDto by filterList
     *
     * @param filterList
     * @param project
     * @return List of filterDto
     */
    private static List<FilterDto> getFilterListByModelMap (Collection<Map<String, Object>> filterList, ProjectDto project) {
        List<FilterDto> result = null;
        log.debug("[INIT] getFilterListByModelMap by project: {}", project);
        if (null == project) {
            log.warn("Project is NULL, not possible to parse List<Map<String,Object>> to List<FilterDto>");
        } else if (null == filterList || filterList.isEmpty()) {
            log.warn("FilterList is empty, not possible to parse List<Map<String,Object>> to List<FilterDto>");
        } else {
            log.debug("Parsing List<Map<String,Object>> to List<FilterDto>");

            List<FilterDto> filterDtoList = new ArrayList<>(filterList.size());
            Iterator<Map<String, Object>> entryIterator = filterList.iterator();
            while (entryIterator.hasNext()) {
                Map<String, Object> filterIterator = entryIterator.next();

                String filterName = (String) filterIterator.get("name");
                if ("rowId".equals(filterName)) {
                    // Don't send rowId to decision view
                    entryIterator.remove();
                    log.debug("Removed filter rowId from filterList");
                } else {
                    FilterDto filter = FilterDto.builder().
                            id((Integer) filterIterator.get("ID")).
                            name(filterName).
                            filterClass((String) filterIterator.get("class")).
                            contactFilter(filterIterator.get("contactFilter").equals(CONTACT_FILTER)).
                            project(project).
                            active(null != filterIterator.get("active") && (filterIterator.get("active").equals(1))).
                            value(null == filterIterator.get("value") ? null : (String) filterIterator.get("value")).
                            build();
                    filterDtoList.add(filter);
                    log.debug("Added filter {}", filter);
                }
            }
            log.debug("Generated List<filterDto>");
            result = filterDtoList;
        }
        log.debug("[END] getFilterListByModelMap by project: {}", project);
        return result;
    }

    /**
     * Generate a FilterCreationDto object based on parameters
     *
     * @param project
     * @param filterList
     * @return FilterCreationDto
     */
    @Override
    public final FilterCreationDto getFilterCreationDto (ProjectDto project, Collection<Map<String, Object>> filterList) {
        log.debug("[INIT] getFilterCreationDto by project: {}", project);
        FilterCreationDto result = null;
        if (null == project) {
            log.warn("Project is NULL, not possible to generate FilterCreationDto");
        } else if (null == filterList || filterList.isEmpty()) {
            log.warn("FilterList is empty, not possible to generate FilterCreationDto");
        } else {
            log.info("Generating FilterCreationDto");
            List<FilterDto> filterDtoList = getFilterListByModelMap(filterList, project);
            log.info("Adding {} filters to FilterCreationDto", filterDtoList.size());
            FilterCreationDto filterCreationDto = new FilterCreationDto(filterDtoList);
            log.info("FilterCreationDto successfully created");
            result = filterCreationDto;
        }
        log.debug("[END] getFilterCreationDto by project: {}", project);
        return result;
    }

    /**
     * Getting contact filter (contactFilter = true) by project
     *
     * @param project
     * @return
     */
    @Override
    public final FilterDto getContactFilter (ProjectDto project) {
        log.info("[INIT] getContactFilter by project: {}", project);
        FilterDto result;
        if (null == project) {
            log.warn("Project is NULL, not possible to get contact filter");
            result = null;
        } else {
            result = getFilterService().getContactFilter(project);
        }
        log.debug("[INIT] getContactFilter by project: {}, result: {}", project, result);
        return result;
    }

    /**
     * Get project's filters
     *
     * @param project
     * @return List of filters
     */
    @Override
    public final List<Map<String, Object>> getFilters (ProjectDto project) {
        log.info("[INIT] Getting filters for project {}", project);
        List<Map<String, Object>> result;
        if (null == project) {
            log.warn("Project is NULL, not possible to get filters");
            result = null;
        } else {
            result = getFilterService().getFilters(project);
        }
        log.info("[END] Getting filters for project {}", project);
        return result;
    }

    /**
     * Update project
     *
     * @param project
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public final void updateProject (ProjectDto project) {
        log.info("[INIT] updateProject {}", project);
        if (null == project) {
            log.warn("Project is NULL, it won't be updated");
        } else {
            getProjectService().updateProject(project);
        }
        log.info("[end] updateProject {}", project);
    }

    /**
     * Update Filters
     *
     * @param filters
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public final void updateFilters (List<FilterDto> filters) {
        log.info("[INIT] Updating filters");
        if (null == filters || filters.isEmpty()) {
            log.warn("No filters found to be updated");
        } else {
            log.info("Updating {} filters", filters.size());
            getFilterService().updateFilters(filters);
            log.info("Updated {} filters successfully", filters.size());
        }
        log.info("[END] Updating filters");
    }

    /**
     * Create and running decision table
     *
     * @param filters
     * @param emailTemplate
     * @param sendEmail
     * @return list of rows that fits filters
     * @throws Exception
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public final List<Map<String, Object>> createDMNModel (List<FilterDto> filters, String emailTemplate, Boolean sendEmail) throws Exception {
        List<Map<String, Object>> res = null;
        ProjectDto project = filters.get(0).getProject();
        if (null == project) {
            log.warn("No project found on filters ");
        } else {
            res = createDMNModel(project, filters, emailTemplate, sendEmail);
        }
        log.info("[END] Creating and running Decision Table");
        return res;
    }

    /**
     * Create and running decision table
     *
     * @param project
     * @param filters
     * @param emailTemplate
     * @param sendEmail
     * @return list of rows that fits filters
     * @throws Exception
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public final List<Map<String, Object>> createDMNModel (ProjectDto project, List<FilterDto> filters, String emailTemplate, Boolean sendEmail) throws Exception {
        List<Map<String, Object>> res;
        log.info("[INIT] Creating and running Decision Table");

        if (null == project) {
            log.warn("No project found on filters ");
            res = null;
        } else {
            log.info("Updating active filters on data base");
            updateFilters(filters);

            log.debug("Send Email enabled? {}", sendEmail);
            project.setEmailTemplate(emailTemplate);

            List<Map<String, Object>> result = bpmnServer.createAndRunDMN(project, this.getActiveFilters(filters), sendEmail);

            log.info("Adding DMN file for project {}", project);
            updateProject(project);

            res = result;
        }
        log.info("[END] Creating and running Decision Table");
        return res;
    }

    /**
     * Get active (active=true) filters
     *
     * @param filters
     * @return list of active filters
     */
    @Override
    public final List<FilterDto> getActiveFilters (List<FilterDto> filters) {
        log.info("[INIT] getActiveFilters");
        List<FilterDto> activeFilters = filters.stream()
                .filter(flt -> (null != flt.getActive() && flt.getActive().equals(Boolean.TRUE)))
                .collect(Collectors.toList());

        if (activeFilters.isEmpty()) {
            log.warn("No active filters found");
        } else {
            log.info("Found {} active filters", activeFilters.size());
            log.info("Active filters {}", activeFilters);
        }
        log.info("[END] getActiveFilters");
        return activeFilters;
    }

    /**
     * Get all projects
     *
     * @return list of projects
     */
    @Override
    public final List<Map<String, Object>> getProjects() {
        List<Map<String, Object>> result = null;
        log.info("[INIT] Getting projects ");
        List<Map<String, Object>> projects = getProjectService().getProjects();
        if (projects.isEmpty()) {
            log.info("No project founds");
        } else {
            result = projects;
        }
        log.info("[END] Got {} projects", projects.size());
        return result;
    }

    /**
     * Get project by selectedProjectId
     *
     * @param selectedProjectId
     * @return project
     */
    @Override
    public final ProjectDto getProject (Integer selectedProjectId) {
        log.info("[INIT] Getting project by Id {}", selectedProjectId);
        ProjectDto result;
        ProjectDto project = getProjectService().getProject(selectedProjectId);
        if (null == project) {
            log.warn("No project found by Id {}", selectedProjectId);
            result = null;
        } else {
            log.info("Found project {}", project);
            result = project;
        }
        log.info("[END] Getting project by Id {}", selectedProjectId);
        return result;
    }

    /**
     * Run DMN for project
     *
     * @param updatedProject
     * @return entities that fit filters defined on DMN file
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public final List<Map<String, Object>> executeDmn (ProjectDto updatedProject) {
        log.info("[INIT] Executing DMN file for project {}", updatedProject);
        List<Map<String, Object>> res;
        List<Map<String, Object>> result = bpmnServer.executeDmn(updatedProject);
        if (null == result || result.isEmpty()) {
            log.warn("No results found after running DMN for project {}", updatedProject);
            res = null;
        } else {
            log.info("Found {} results after running DMN for project {}", result.size(), updatedProject);
            res = result;
        }
        log.info("[END] Executing DMN file for project {}", updatedProject);
        return res;
    }

    /**
     * Validate DMN file
     *
     * @param project
     * @param bytes
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public final void validateDmn (ProjectDto project, byte[] bytes) {
        log.info("[INIT] Validating DMN file");
        bpmnServer.validateDmn(bytes);
        log.info("Validated DMN file");

        project.setDmnFile(bytes);
        log.info("Added new DMN file, updating project");
        getProjectService().updateProject(project);
        log.info("[END] Successfully added DMN file for project {}", project);
    }

    /**
     * Get project's row data
     *
     * @param project
     * @return
     */
    @Override
    public final List<Map<String, Object>> getRowData (ProjectDto project) {
        log.info("[INIT] get row data for project: {}", project);
        List<Map<String, Object>> result = null;
        if (null == project) {
            log.warn("Project is null");
        } else {
            log.info("Getting all info from table: {}", project.getRowDataTableName());
            List<Map<String, Object>> entities = getRowDataService().getRowData(project);
            if (null == entities || entities.isEmpty()) {
                log.info("Not found data from project {}", project);
            } else {
                log.info("Got all info from table: {}", project.getRowDataTableName());
                result = entities;
            }
        }
        log.info("[END] get row data for project: {}", project);
        return result;
    }

    /**
     * Delete all project's row data
     *
     * @param project
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public final void deleteRowData (ProjectDto project) {
        log.info("[INIT] deleteRowData for project: {}", project);
        if (null == project) {
            log.warn("Project is null");
        } else {
            log.info("Deleting all registers for project {}", project);
            getRowDataService().deleteRowData(project);
            log.info("Registers successfully deleted for project {}", project);
        }
        log.info("[END] deleteRowData for project: {}", project);
    }

    /**
     * Delete project
     *
     * @param project
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public final void deleteProject (ProjectDto project) {
        log.info("[INIT] delete project :{}", project);
        if (null == project) {
            log.warn("Project is null");
        } else {
            log.info("Deleting project {}", project);
            getProjectService().deleteProject(project);
        }
        log.info("[END] delete project :{}", project);
    }

    /**
     * Add project's filters to modelAndView
     *
     * @param modelAndView
     * @param project
     */
    @Override
    public final void addFilterInformationToModel (ModelAndView modelAndView, ProjectDto project) {
        log.debug("[INIT] addFilterInformationToModel for project: {}", project);
        Map<String, List<Map<String, Object>>> filters = getFiltersModelMap(project);

        if (null == filters || filters.isEmpty()) {
            log.error("No filters found");
            modelAndView.getModel().put("message", "No filters found");
        } else {
            log.error("Found {} filters", filters.size());
            modelAndView.addAllObjects(filters);
            modelAndView.addObject("form", getFilterCreationDto(project, filters.get("filterList")));
            modelAndView.addObject("contactFilter", getContactFilter(project));
        }
        log.debug("[END] addFilterInformationToModel for project: {}", project);
    }

    /**
     * Get Project's filters Map
     *
     * @param project
     * @return filters Map
     */
    private Map<String, List<Map<String, Object>>> getFiltersModelMap (ProjectDto project) {
        log.debug("[INIT] getFiltersModelMap for project: {}", project);
        Map<String, List<Map<String, Object>>> result;
        List<Map<String, Object>> filterList = getFilters(project);

        if (null == filterList || filterList.isEmpty()) {
            log.error("No filters found");
            result = null;
        } else {
            log.info("found {} filters", filterList.size());
            Map<String, List<Map<String, Object>>> modelMap = new HashMap<>(1);
            modelMap.put("filterList", filterList);
            result = modelMap;
        }
        log.debug("[END] getFiltersModelMap for project: {}", project);
        return result;
    }

    /**
     * Get existing projects
     *
     * @return List of projects
     */
    @Override
    public Map<String, List<Map<String, Object>>> getExistingProjects() {
        log.info("[INIT] getExistingProjects");
        Map<String, List<Map<String, Object>>> modelMap = null;
        List<Map<String, Object>> projects = getProjects();
        if (projects == null) {
            log.warn("No projects founds");
        } else {
            modelMap = new HashMap<>();
            log.info("Found {} projects", projects.size());
            modelMap.put("existingProjects", projects);
        }
        log.info("[END] getExistingProjects");
        return modelMap;
    }
}
