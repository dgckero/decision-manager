/*
  @author david
 */

package com.dgc.dm.core.service.db;

import com.dgc.dm.core.dto.ProjectDto;
import org.hibernate.exception.GenericJDBCException;

import java.util.List;
import java.util.Map;

public interface ProjectService {
    ProjectDto createProject(String projectName) throws GenericJDBCException;

    void updateProject(ProjectDto project);

    List<Map<String, Object>> getProjects();

    ProjectDto getProject(Integer selectedProjectId);

    void deleteProject(ProjectDto project);
}
