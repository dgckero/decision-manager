/*
  @author david
 */

package com.dgc.dm.core.db.dao;

import com.dgc.dm.core.db.model.Project;

import java.util.List;

public interface ProjectDao {

    Project createProject(String projectName);

    void updateProject(Project project);

    List<Project> getProjects();

    Project getProject(Integer selectedProjectId);

    void deleteProject(Project project);

}
