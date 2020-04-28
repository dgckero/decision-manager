/*
  @author david
 */

package com.dgc.dm.core.service.db;

import com.dgc.dm.core.dto.ProjectDto;

import java.util.List;
import java.util.Map;

public interface RowDataService {
    void createRowDataTable(Map<String, Class<?>> columns, ProjectDto project);

    void persistRowData(String insertSentence, List<Object[]> infoToBePersisted);

    List<Map<String, Object>> getRowData(ProjectDto project);

    void deleteRowData(ProjectDto project);

    Integer getRowDataSize(ProjectDto project);
}
