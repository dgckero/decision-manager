/*
  @author david
 */

package com.dgc.dm.core.db.dao;

import com.dgc.dm.core.db.model.Project;

import java.util.List;
import java.util.Map;

public interface RowDataDao {

    void createRowDataTable (Map<String, Class<?>> columns, Project project);

    void persistRowData (String insertSentence, List<Object[]> infoToBePersisted);

    List<Map<String, Object>> getRowData (Project project);

    void deleteRowData (Project project);

    Integer getRowDataSize (Project map);

}
