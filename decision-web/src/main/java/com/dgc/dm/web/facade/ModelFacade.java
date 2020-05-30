/*
  @author david
 */

package com.dgc.dm.web.facade;

import com.dgc.dm.core.dto.FilterCreationDto;
import com.dgc.dm.core.dto.FilterDto;
import com.dgc.dm.core.dto.ProjectDto;
import com.dgc.dm.core.exception.DecisionException;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface ModelFacade {
    FilterCreationDto getFilterCreationDto(ProjectDto project, List<FilterDto> filterList);

    FilterDto getContactFilter(ProjectDto project);

    List<FilterDto> getFilters(ProjectDto project);

    void updateProject(ProjectDto project);

    void updateFilters(List<FilterDto> activeFilters);

    List<Map<String, Object>> createDMNModel(ProjectDto project, List<FilterDto> filters, String emailTemplate, Boolean sendEmail) throws IOException;

    List<Map<String, Object>> createDMNModel(List<FilterDto> filters, String emailTemplate, Boolean sendEmail) throws DecisionException, IOException;

    List<FilterDto> getActiveFilters(List<FilterDto> filters);

    List<ProjectDto> getProjects();

    ProjectDto getProject(Integer selectedProjectId);

    List<Map<String, Object>> executeDmn(ProjectDto updatedProject);

    void validateDmn(ProjectDto project, byte[] bytes);

    List<Map<String,Object>> getRowData(ProjectDto project);

    void deleteRowData(ProjectDto project);

    void deleteProject(ProjectDto project);

    void addFilterInformationToModel(ModelAndView modelAndView, ProjectDto project);

    Map<String, List<ProjectDto>> getExistingProjects();

    ProjectDto createProjectModel(String projectName, Map<String, Class<?>> colMapByName);

    void persistRowData(String insertSentence, List<Object[]> infoToBePersisted);

    int getRowDataSize(ProjectDto project);
}
