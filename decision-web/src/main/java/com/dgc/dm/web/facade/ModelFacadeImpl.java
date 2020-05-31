/*
  @author david
 */

package com.dgc.dm.web.facade;

import com.dgc.dm.core.dto.FilterCreationDto;
import com.dgc.dm.core.dto.FilterDto;
import com.dgc.dm.core.dto.ProjectDto;
import com.dgc.dm.core.exception.DecisionException;
import com.dgc.dm.core.service.bpmn.BPMNServer;
import com.dgc.dm.core.service.db.FilterService;
import com.dgc.dm.core.service.db.ProjectService;
import com.dgc.dm.core.service.db.RowDataService;
import lombok.extern.log4j.Log4j2;
import org.hibernate.exception.GenericJDBCException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.ModelAndView;
import org.sqlite.SQLiteErrorCode;

import javax.persistence.PersistenceException;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Log4j2
@Service
class ModelFacadeImpl implements ModelFacade {

    private static final Integer ACTIVE_FILTER = 1;
    private static final Collection<String> OMITTED_DATA = Stream.of("rowId", "project", "dataCreationDate", "lastUpdatedDate").collect(Collectors.toList());
    public static final String PROJECT_IS_NULL = "Project is null";
    public static final String NO_FILTERS_FOUND = "No filters found";
    public static final String CONTACT_FILTER = "contactFilter";
    public static final String FILTER_ACTIVE = "active";
    public static final String FILTER_VALUE = "value";

    private final BPMNServer bpmnServer;

    private final RowDataService rowDataService;

    private final ProjectService projectService;

    private final FilterService filterService;

    /**
     * Initialize bpmnServer
     *
     * @param bpmnServer
     */
    public ModelFacadeImpl(BPMNServer bpmnServer, RowDataService rowDataService, ProjectService projectService, FilterService filterService) {
        this.bpmnServer = bpmnServer;
        this.rowDataService = rowDataService;
        this.projectService = projectService;
        this.filterService = filterService;
    }

    /**
     * Get list of FilterDto by filterList
     *
     * @param filterList
     * @param project
     * @return List of filterDto
     */
    private static List<FilterDto> getFilterListByModelMap(final Collection<Map<String, Object>> filterList, final ProjectDto project) {
        log.debug("[INIT] getFilterListByModelMap by project: {}", project);

        final List<FilterDto> filterDtoList = new ArrayList<>(filterList.size());
        final Iterator<Map<String, Object>> entryIterator = filterList.iterator();
        while (entryIterator.hasNext()) {
            final Map<String, Object> filterIterator = entryIterator.next();

            final String filterName = (String) filterIterator.get("name");
            if (OMITTED_DATA.contains(filterName)) {
                // Don't send to decision view
                entryIterator.remove();
                log.debug("Removed filter " + filterName + " from filterList");
            } else {
                final FilterDto filter = FilterDto.builder().
                        id((Integer) filterIterator.get("ID")).
                        name(filterName).
                        filterClass((String) filterIterator.get("class")).
                        contactFilter(null != filterIterator.get(CONTACT_FILTER) && (filterIterator.get(CONTACT_FILTER).equals(ACTIVE_FILTER))).
                        project(project).
                        active(null != filterIterator.get(FILTER_ACTIVE) && (filterIterator.get(FILTER_ACTIVE).equals(ACTIVE_FILTER))).
                        value(null == filterIterator.get(FILTER_VALUE) ? null : (String) filterIterator.get(FILTER_VALUE)).
                        build();
                filterDtoList.add(filter);
                log.debug("Added filter {}", filter);
            }
        }

        log.debug("[END] getFilterListByModelMap by project: {}", project);
        return filterDtoList;
    }

    /**
     * Generate a FilterCreationDto object based on parameters
     *
     * @param project
     * @param filterList
     * @return FilterCreationDto
     */
    @Override
    public final FilterCreationDto getFilterCreationDto(final ProjectDto project, final Collection<Map<String, Object>> filterList) {
        log.debug("[INIT] getFilterCreationDto by project: {}", project);
        FilterCreationDto result = null;
        if (null == project) {
            log.warn("Project is NULL, not possible to generate FilterCreationDto");
        } else if (null == filterList || filterList.isEmpty()) {
            log.warn("FilterList is empty, not possible to generate FilterCreationDto");
        } else {
            log.info("Generating FilterCreationDto");
            final List<FilterDto> filterDtoList = getFilterListByModelMap(filterList, project);
            if (filterDtoList.isEmpty()) {
                log.warn("No filters found for project {}", project);
            } else {
                log.info("Adding {} filters to FilterCreationDto", filterDtoList.size());
                final FilterCreationDto filterCreationDto = new FilterCreationDto(filterDtoList);
                log.info("FilterCreationDto successfully created");
                result = filterCreationDto;
            }
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
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public FilterDto getContactFilter(final ProjectDto project) {
        log.info("[INIT] getContactFilter by project: {}", project);
        final FilterDto result;
        if (null == project) {
            log.warn("Project is NULL, not possible to get contact filter");
            result = null;
        } else {
            result = filterService.getContactFilter(project);
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
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public final List<Map<String, Object>> getFilters(final ProjectDto project) {
        log.info("[INIT] Getting filters for project {}", project);
        final List<Map<String, Object>> result;
        if (null == project) {
            log.warn("Project is NULL, not possible to get filters");
            result = null;
        } else {
            result = filterService.getFilters(project);
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
    public void updateProject(final ProjectDto project) {
        log.info("[INIT] updateProject {}", project);
        if (null == project) {
            log.warn("Project is NULL, it won't be updated");
        } else {
            projectService.updateProject(project);
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
    public void updateFilters(final List<FilterDto> filters) {
        log.info("[INIT] Updating filters");
        if (null == filters || filters.isEmpty()) {
            log.warn("No filters found to be updated");
        } else {
            log.info("Updating {} filters", filters.size());
            filterService.updateFilters(filters);
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
    public List<Map<String, Object>> createDMNModel(final List<FilterDto> filters, final String emailTemplate, final Boolean sendEmail) throws DecisionException, IOException {
        List<Map<String, Object>> res = null;
        if (null == filters || filters.isEmpty()) {
            log.error("No filters found to be updated");
        } else {
            final ProjectDto project = filters.get(0).getProject();
            if (null == project) {
                log.error("No project found on filters ");
            } else {
                res = this.createDMNModel(project, filters, emailTemplate, sendEmail);
            }
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
    public List<Map<String, Object>> createDMNModel(final ProjectDto project, final List<FilterDto> filters, final String emailTemplate, final Boolean sendEmail) throws IOException {
        log.info("[INIT] Creating and running Decision Table");

        this.updateFilters(filters);
        log.debug("Send Email enabled? {}", sendEmail);
        project.setEmailTemplate(emailTemplate);

        final List<Map<String, Object>> result = this.bpmnServer.createAndRunDMN(project, getActiveFilters(filters), sendEmail);
        log.info("Adding DMN file for project {}", project);
        this.updateProject(project);

        log.info("[END] Creating and running Decision Table");
        return result;
    }

    /**
     * Get active (active=true) filters
     *
     * @param filters
     * @return list of active filters
     */
    @Override
    public final List<FilterDto> getActiveFilters(final List<FilterDto> filters) {
        log.info("[INIT] getActiveFilters");
        final List<FilterDto> activeFilters = filters.stream()
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
        final List<Map<String, Object>> projects = projectService.getProjects();
        if (null == projects || projects.isEmpty()) {
            log.info("[END] No project founds");
        } else {
            result = projects;
            log.info("[END] Got {} projects", projects.size());
        }
        return result;
    }

    /**
     * Get project by selectedProjectId
     *
     * @param selectedProjectId
     * @return project
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public ProjectDto getProject(final Integer selectedProjectId) {
        log.info("[INIT] Getting project by Id {}", selectedProjectId);
        final ProjectDto result;
        final ProjectDto project = projectService.getProject(selectedProjectId);
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
    public List<Map<String, Object>> executeDmn(final ProjectDto updatedProject) {
        log.info("[INIT] Executing DMN file for project {}", updatedProject);
        final List<Map<String, Object>> res;
        final List<Map<String, Object>> result = this.bpmnServer.executeDmn(updatedProject);
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
    public void validateDmn(final ProjectDto project, final byte[] bytes) {
        log.info("[INIT] Validating DMN file");
        this.bpmnServer.validateDmn(bytes);
        log.info("[END] Validated DMN file");
    }

    /**
     * Get project's row data
     *
     * @param project
     * @return
     */
    @Override
    public final List<Map<String, Object>> getRowData(final ProjectDto project) {
        log.info("[INIT] get row data for project: {}", project);
        List<Map<String, Object>> result = null;
        if (null == project) {
            log.warn(PROJECT_IS_NULL);
        } else {
            log.info("Getting all info from table: {}", project.getRowDataTableName());
            final List<Map<String, Object>> entities = rowDataService.getRowData(project);
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
    public void deleteRowData(final ProjectDto project) {
        log.info("[INIT] deleteRowData for project: {}", project);
        if (null == project) {
            log.warn(PROJECT_IS_NULL);
        } else {
            log.info("Deleting all registers for project {}", project);
            rowDataService.deleteRowData(project);
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
    public void deleteProject(final ProjectDto project) {
        log.info("[INIT] delete project :{}", project);
        if (null == project) {
            log.warn(PROJECT_IS_NULL);
        } else {
            log.info("Deleting project {}", project);
            projectService.deleteProject(project);
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
    public final void addFilterInformationToModel(final ModelAndView modelAndView, final ProjectDto project) {
        log.debug("[INIT] addFilterInformationToModel for project: {}", project);
        final Map<String, List<Map<String, Object>>> filters = this.getFiltersModelMap(project);

        if (null == filters || filters.isEmpty()) {
            log.error(NO_FILTERS_FOUND);
            modelAndView.getModel().put("message", NO_FILTERS_FOUND);
        } else {
            log.info("Found {} filters", filters.size());
            modelAndView.addAllObjects(filters);
            modelAndView.addObject("form", this.getFilterCreationDto(project, filters.get("filterList")));
            modelAndView.addObject(CONTACT_FILTER, this.getContactFilter(project));
        }
        log.debug("[END] addFilterInformationToModel for project: {}", project);
    }

    /**
     * Get Project's filters Map
     *
     * @param project
     * @return filters Map
     */
    private Map<String, List<Map<String, Object>>> getFiltersModelMap(final ProjectDto project) {
        log.debug("[INIT] getFiltersModelMap for project: {}", project);
        final Map<String, List<Map<String, Object>>> result;
        final List<Map<String, Object>> filterList = this.getFilters(project);

        if ((null == filterList) || filterList.isEmpty()) {
            log.error(NO_FILTERS_FOUND);
            result = null;
        } else {
            log.info("found {} filters", filterList.size());
            final Map<String, List<Map<String, Object>>> modelMap = new HashMap<>(1);
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

        List<Map<String, Object>> projects = this.getProjects();

        if (null == projects) {
            log.warn("No projects founds");
        } else {
            modelMap = new HashMap<>();
            log.info("Found {} projects", projects.size());
            modelMap.put("existingProjects", projects);
        }
        log.info("[END] getExistingProjects");
        return modelMap;
    }

    /**
     * Create Project and Data tables
     * Add Project information on Project table
     *
     * @param projectName
     * @param colMapByName
     * @return new Project
     */
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public ProjectDto createProjectModel(String projectName, Map<String, Class<?>> colMapByName) {
        log.info("[INIT] createProjectModel by projectName: {}", projectName);
        ProjectDto project = null;
        try {
            project = projectService.createProject(projectName);
            rowDataService.createRowDataTable(colMapByName, project);
            filterService.persistFilterList(generateFilterList(colMapByName, project), project);
        } catch (PersistenceException e) {
            log.error("Error Creating project : {}", e.getMessage());
            if ((e.getCause() instanceof GenericJDBCException) && ((GenericJDBCException) e.getCause()).getErrorCode() == SQLiteErrorCode.SQLITE_CONSTRAINT.code) {
                throw new DecisionException("Ya existe un proyecto con nombre " + projectName + ", por favor utilice un nombre diferente");
            }
        }
        log.info("[END] createProjectModel project: {}", project);
        return project;
    }

    /**
     * Generate filter list based on project's columns
     *
     * @param columns
     * @param project
     * @return
     */
    private static List<FilterDto> generateFilterList(Map<String, Class<?>> columns, ProjectDto project) {
        log.info("[INIT] generateFilterList by project: {}", project);

        List<FilterDto> filterList = new ArrayList<>();
        for (Map.Entry<String, Class<?>> column : columns.entrySet()) {
            if (!StringUtils.isEmpty(column.getValue())) {
                filterList.add(FilterDto.builder().
                        name(column.getKey()).
                        filterClass(column.getValue().getSimpleName()).
                        active(Boolean.FALSE).
                        contactFilter(Boolean.FALSE).
                        project(project).
                        build());
            }
        }
        log.info("[END] generateFilterList by project: {}", project);
        return filterList;
    }

    /**
     * Persist row data
     *
     * @param insertSentence
     * @param infoToBePersisted
     */
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void persistRowData(String insertSentence, List<Object[]> infoToBePersisted) {
        log.info("[INIT] persistRowData");

        rowDataService.persistRowData(insertSentence, infoToBePersisted);

        log.info("[END] persistRowData");
    }

    /**
     * Get number of rows on table project.RowDataTableName
     *
     * @param project
     * @return number of rows on table project.RowDataTableName
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public int getRowDataSize(ProjectDto project) {
        log.info("[INIT] getRowDataSize by project: {}", project);

        int rowDataSize = rowDataService.getRowDataSize(project);

        log.info("[END] persistRowData by project: {}, rowDataSize: {}", project, rowDataSize);
        return rowDataSize;
    }
}
