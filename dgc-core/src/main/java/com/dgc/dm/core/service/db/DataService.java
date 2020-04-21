/*
  @author david
 */

package com.dgc.dm.core.service.db;

import com.dgc.dm.core.dto.ProjectDto;

import java.util.List;
import java.util.Map;

public interface DataService {
    void createDataTable(Map<String, Class<?>> columns, ProjectDto project);

    void persistData(String insertSentence, List<Object[]> infoToBePersisted);

    List<Map<String, Object>> getCommonData(ProjectDto project);

    void deleteCommonData(ProjectDto project);

    Integer getCommonDataSize(ProjectDto project);
}
