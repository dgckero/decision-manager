/*
  @author david
 */

package com.dgc.dm.core.db.dao;

import com.dgc.dm.core.db.model.Project;
import org.springframework.jdbc.UncategorizedSQLException;

import java.util.List;
import java.util.Map;

public interface ProjectDao {

    Project createProject (String projectName);

    void updateProject (Project project);

    List<Map<String, Object>> getProjects ( ) throws UncategorizedSQLException;

    Project getProject (Integer selectedProjectId);

    void deleteProject (Project project);

}
