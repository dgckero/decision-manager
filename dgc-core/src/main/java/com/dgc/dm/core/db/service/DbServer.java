/*
  @author david
 */

package com.dgc.dm.core.db.service;

import com.dgc.dm.core.db.model.Project;
import com.dgc.dm.core.dto.FilterDto;
import com.dgc.dm.core.dto.ProjectDto;

import java.util.List;
import java.util.Map;

public interface DbServer {
    void createAndPopulateFilterTable(final Map<String, Class<?>> props, final ProjectDto project);

    void persistExcelRows(final String insertSentence, final List<Object[]> infoToBePersisted);

    List<Map<String, Object>> getFilters();

    void updateFilters(List<FilterDto> activeFilters);

    List<Map<String, Object>> getCommonData();

    Project createProject(String projectName);
}
