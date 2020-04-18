/*
  @author david
 */

package com.dgc.dm.web.service;

import com.dgc.dm.core.dto.FilterCreationDto;
import com.dgc.dm.core.dto.FilterDto;
import com.dgc.dm.core.dto.ProjectDto;
import com.dgc.dm.core.service.bpmn.BPMNServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.*;
import java.util.stream.Collectors;


@Slf4j
@Service
class ModelFacadeImpl extends CommonFacade implements ModelFacade {

    @Autowired
    private BPMNServer bpmnServer;

    private static final Integer CONTACT_FILTER = 1;

    private static boolean sendEmailToContact(List<FilterDto> filters) {
        final boolean sendEmail = filters.stream()
                .anyMatch(flt -> (null != flt.getContactFilter() && flt.getContactFilter().equals(Boolean.TRUE)));

        log.info("Send email enabled: {}", sendEmail);
        return sendEmail;
    }

    private static List<FilterDto> getFilterListByModelMap(Collection<Map<String, Object>> filterList, ProjectDto project) {
        if (null == project) {
            log.warn("Project is NULL, not possible to parse List<Map<String,Object>> to List<FilterDto>");
            return null;
        } else if (null == filterList || filterList.isEmpty()) {
            log.warn("FilterList is empty, not possible to parse List<Map<String,Object>> to List<FilterDto>");
            return null;
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
                            build();
                    filterDtoList.add(filter);
                    log.debug("Added filter {}", filter);
                }
            }
            log.debug("Generated List<filterDto>");
            return filterDtoList;
        }
    }

    @Override
    public final FilterCreationDto getFilterCreationDto(ProjectDto project, Collection<Map<String, Object>> filterList) {
        if (null == project) {
            log.warn("Project is NULL, not possible to generate FilterCreationDto");
            return null;
        } else if (null == filterList || filterList.isEmpty()) {
            log.warn("FilterList is empty, not possible to generate FilterCreationDto");
            return null;
        } else {
            log.info("Generating FilterCreationDto");
            final List<FilterDto> filterDtoList = getFilterListByModelMap(filterList, project);
            log.info("Adding {} filters to FilterCreationDto", filterDtoList.size());
            FilterCreationDto filterCreationDto = new FilterCreationDto(filterDtoList);
            log.info("FilterCreationDto successfully created");
            return filterCreationDto;
        }
    }

    @Override
    public final FilterDto getContactFilter(ProjectDto project) {
        if (null == project) {
            log.warn("Project is NULL, not possible to get contact filter");
            return null;
        } else {
            log.info("Getting contact filter for project {}", project);
            return this.getFilterService().getContactFilter(project);
        }
    }

    @Override
    public List<Map<String, Object>> getFilters(ProjectDto project) {
        if (null == project) {
            log.warn("Project is NULL, not possible to get filters");
            return null;
        } else {
            log.info("Getting filters for project {}", project);
            List<Map<String, Object>> filterList = this.getFilterService().getFilters(project);
            return filterList;
        }
    }

    @Override
    public void updateProject(ProjectDto project) {
        if (null == project) {
            log.warn("Project is NULL, it won't be updated");
        } else {
            log.info("Updating project {}", project);
            getProjectService().updateProject(project);
        }
    }

    @Override
    public void updateFilters(List<FilterDto> filters) {
        if (null == filters || filters.isEmpty()) {
            log.warn("No filters found to be updated");
        } else {
            log.info("Updating {} filters", filters.size());
            getFilterService().updateFilters(filters);
            log.info("Updated {} filters successfully", filters.size());
        }
    }

    @Override
    @Transactional
    public List<Map<String, Object>> createBPMNModel(List<FilterDto> activeFilters, String emailTemplate) throws Exception {
        ProjectDto project = activeFilters.get(0).getProject();
        if (null == project) {
            log.warn("No project found on filters ");
            return null;
        } else {
            log.info("Creating and running Decision Table for project " + project);

            if (null != emailTemplate) {
                log.info("Adding emailTemplate {} to project {}", emailTemplate, project);
                project.setEmailTemplate(emailTemplate);
                updateProject(project);
            }
            log.info("Updating active filters on data base");
            updateFilters(activeFilters);

            List<Map<String, Object>> result = bpmnServer.createAndRunDMN(project, activeFilters, sendEmailToContact(activeFilters));

            log.info("Adding DMN file for project {}", project);
            getProjectService().updateProject(project);

            return result;
        }
    }

    @Override
    public List<FilterDto> getActiveFilters(List<FilterDto> filters) {
        List<FilterDto> activeFilters = filters.stream()
                .filter(flt -> (null != flt.getActive() && flt.getActive().equals(Boolean.TRUE)))
                .collect(Collectors.toList());

        if (activeFilters.isEmpty()) {
            log.warn("No active filters found");
        } else {
            log.info("Found {} active filters", activeFilters.size());
            log.info("Active filters {}", activeFilters);
        }
        return activeFilters;
    }

    @Override
    public List<Map<String, Object>> getProjects() {
        log.info("Getting projects ");
        List<Map<String, Object>> projects = getProjectService().getProjects();
        if (projects.isEmpty()) {
            log.info("No project founds");
            return null;
        }
        log.info("Got {} projects", projects.size());
        return projects;
    }

    @Override
    public ProjectDto getProject(Integer selectedProjectId) {
        log.info("Getting project by Id {}", selectedProjectId);
        ProjectDto project = getProjectService().getProject(selectedProjectId);
        if (null == project) {
            log.warn("No project found by Id {}", selectedProjectId);
            return null;
        } else {
            log.info("Found project {}", project);
            return project;
        }
    }

    @Override
    public List<Map<String, Object>> executeDmn(final ProjectDto updatedProject) {
        log.info("Executing DMN file for project {}", updatedProject);
        List<Map<String, Object>> result = bpmnServer.executeDmn(updatedProject);
        if (null == result || result.isEmpty()) {
            log.warn("No results found after running DMN for project {}", updatedProject);
            return null;
        } else {
            log.info("Found {} results after running DMN for project {}", result.size(), updatedProject);
            return result;
        }
    }

    @Transactional
    @Override
    public void validateDmn(ProjectDto project, final byte[] bytes) {
        log.info("Validating DMN file");
        bpmnServer.validateDmn(bytes);
        log.info("Validated DMN file");

        project.setDmnFile(bytes);
        log.info("Added new DMN file, updating project");
        getProjectService().updateProject(project);
        log.info("Successfully added DMN file for project {}", project);
    }

    @Override
    public List<Map<String, Object>> getCommonData(final ProjectDto project) {
        if (null == project) {
            log.warn("Project is null");
            return null;
        } else {
            log.info("Getting all info from table: {}", project.getCommonDataTableName());
            List<Map<String, Object>> entities = getDataService().getCommonData(project);
            if (null == entities || entities.isEmpty()) {
                log.info("Not found data from project {}", project);
                return null;
            } else {
                log.info("Got all info from table: {}", project.getCommonDataTableName());
                return entities;
            }
        }
    }

    @Override
    public void deleteCommonData(final ProjectDto project) {
        if (null == project) {
            log.warn("Project is null");
        } else {
            log.info("Deleting all registers for project {}", project);
            getDataService().deleteCommonData(project);
            log.info("Registers successfully deleted for project {}", project);
        }
    }

    @Override
    public void deleteProject(final ProjectDto project) {
        if (null == project) {
            log.warn("Project is null");
        } else {
            log.info("Deleting project {}", project);
            getProjectService().deleteProject(project);
            log.info("Deleted project {}", project);
        }
    }
}
