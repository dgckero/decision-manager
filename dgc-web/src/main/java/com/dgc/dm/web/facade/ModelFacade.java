/*
  @author david
 */

package com.dgc.dm.web.facade;

import com.dgc.dm.core.dto.FilterCreationDto;
import com.dgc.dm.core.dto.FilterDto;
import com.dgc.dm.core.dto.ProjectDto;
import org.springframework.web.servlet.ModelAndView;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface ModelFacade {
    FilterCreationDto getFilterCreationDto(ProjectDto project, Collection<Map<String, Object>> filterList);

    FilterDto getContactFilter(ProjectDto project);

    List<Map<String, Object>> getFilters(ProjectDto project);

    void updateProject(ProjectDto project);

    void updateFilters(List<FilterDto> activeFilters);

    List<Map<String, Object>> createDMNModel(List<FilterDto> filters, String emailTemplate, Boolean sendEmail) throws Exception;

    List<FilterDto> getActiveFilters(List<FilterDto> filters);

    List<Map<String, Object>> getProjects();

    ProjectDto getProject(Integer selectedProjectId);

    List<Map<String, Object>> executeDmn(ProjectDto updatedProject);

    void validateDmn(ProjectDto project, byte[] bytes);

    List<Map<String, Object>> getRowData(ProjectDto project);

    void deleteRowData(ProjectDto project);

    void deleteProject(ProjectDto project);

    void addFilterInformationToModel(ModelAndView modelAndView, ProjectDto project);

    Map<String, List<Map<String, Object>>> getExistingProjects();
}
