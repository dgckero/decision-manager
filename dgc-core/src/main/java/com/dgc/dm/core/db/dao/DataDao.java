/*
  @author david
 */

package com.dgc.dm.core.db.dao;

import com.dgc.dm.core.db.model.Project;

import java.util.List;
import java.util.Map;

public interface DataDao {

    void createDataTable(Map<String, Class<?>> columns, Project project);

    void persistData(String insertSentence, List<Object[]> infoToBePersisted);

    List<Map<String, Object>> getCommonData(Project project);

    void deleteCommonData(Project project);
}
