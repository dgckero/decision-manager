/*
  @author david
 */

package com.dgc.dm.core.service.db;

import com.dgc.dm.core.dto.ProjectDto;
import org.hibernate.exception.GenericJDBCException;

import java.util.List;

public interface ProjectService {
    ProjectDto createProject(String projectName) throws GenericJDBCException;

    void updateProject(ProjectDto project);

    List<ProjectDto> getProjects();

    ProjectDto getProject(Integer selectedProjectId);

    void deleteProject(ProjectDto project);
}
