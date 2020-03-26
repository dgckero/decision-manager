/*
  @author david
 */

package com.dgc.dm.core.db.service;

import com.dgc.dm.core.db.model.Filter;
import com.dgc.dm.core.db.model.Project;
import com.dgc.dm.core.dto.FilterDto;
import com.dgc.dm.core.dto.ProjectDto;

import java.util.List;
import java.util.Map;

public interface DbServer {

    void createFilterTable(ProjectDto project);

    List<Filter> createCommonDatasTable(Map<String, Class<?>> columns, ProjectDto project);

    void persistFilterList(List<Filter> filterList);

    void persistExcelRows(final String insertSentence, final List<Object[]> infoToBePersisted);

    List<Map<String, Object>> getFilters();

    List<Map<String, Object>> getFilters(ProjectDto project);

    void updateFilters(List<FilterDto> activeFilters);

    List<Map<String, Object>> getCommonData();

    List<Map<String, Object>> getCommonData(ProjectDto project);

    Project createProject(String projectName);
}
