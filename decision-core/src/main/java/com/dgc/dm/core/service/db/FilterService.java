/*
  @author david
 */

package com.dgc.dm.core.service.db;

import com.dgc.dm.core.dto.FilterDto;
import com.dgc.dm.core.dto.ProjectDto;

import java.util.List;

public interface FilterService {

    void persistFilterList(List<FilterDto> filterList, ProjectDto project);

    List<FilterDto> getFilters(ProjectDto project);

    void updateFilters(List<FilterDto> filters);

    FilterDto getContactFilter(ProjectDto project);
}
