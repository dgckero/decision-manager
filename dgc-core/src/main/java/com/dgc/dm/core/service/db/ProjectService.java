/*
  @author david
 */

package com.dgc.dm.core.service.db;

import com.dgc.dm.core.dto.ProjectDto;

public interface ProjectService {
    ProjectDto createProject(String projectName);
}
