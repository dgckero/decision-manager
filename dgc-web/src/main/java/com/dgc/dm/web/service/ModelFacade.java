/*
  @author david
 */

package com.dgc.dm.web.service;

import com.dgc.dm.core.dto.FilterCreationDto;
import com.dgc.dm.core.dto.FilterDto;
import com.dgc.dm.core.dto.ProjectDto;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface ModelFacade {
    FilterCreationDto getFilterCreationDto(ProjectDto project, Collection<Map<String, Object>> filterList);

    FilterDto getContactFilter(ProjectDto project);

    List<Map<String, Object>> getFilters(ProjectDto project);
}