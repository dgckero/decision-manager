/*
  @author david
 */

package com.dgc.dm.core.service.db;

import com.dgc.dm.core.db.dao.ProjectDao;
import com.dgc.dm.core.db.model.Project;
import com.dgc.dm.core.dto.ProjectDto;
import lombok.extern.log4j.Log4j2;
import org.hibernate.exception.GenericJDBCException;
import org.hibernate.hql.internal.ast.QuerySyntaxException;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.sqlite.SQLiteException;

import java.util.ArrayList;
import java.util.List;

@Log4j2
@Service
public class ProjectServiceImpl extends CommonServer implements ProjectService {

    @Autowired
    private ProjectDao projectDao;

    /**
     * Check if Exception is [SQLITE_ERROR] SQL error or missing database
     *
     * @param e
     * @return true if Exception is SQLITE_ERROR
     */
    private static boolean isProjectNotFoundException(QuerySyntaxException e) {
        return
                e.getMessage() != null && e.getMessage().contains("Projects is not mapped");

    }

    /**
     * Create project based on projectName
     *
     * @param projectName
     * @return project
     */
    @Override
    public ProjectDto createProject(String projectName) throws GenericJDBCException {
        log.debug("[INIT] createProject " + projectName);
        Project projectEntity = projectDao.createProject(projectName);
        ProjectDto project = getModelMapper().map(projectEntity, ProjectDto.class);
        log.debug("[END] Project " + project + " successfully created");
        return project;
    }

    /**
     * Update project
     *
     * @param project
     */
    @Override
    public void updateProject(final ProjectDto project) {
        log.debug("[INIT] Updating project {}", project);
        final Project projectEntity = this.getModelMapper().map(project, Project.class);
        this.projectDao.updateProject(projectEntity);
        log.debug("[END] Project {} successfully updated", project);
    }

    /**
     * Get all projects
     *
     * @return list of projects
     * @throws SQLiteException
     */
    @Override
    public final List<ProjectDto> getProjects() {
        List<ProjectDto> result;
        log.debug("[INIT] Getting projects ");
        try {
            final List<Project> projectsEntity = this.projectDao.getProjects();
            if (projectsEntity.isEmpty()) {
                log.warn("No project founds");
            } else {
                log.debug("[END] Got {} projects", projectsEntity.size());
            }
            result = getModelMapper().map(projectsEntity, (new TypeToken<List<ProjectDto>>() {
            }.getType()));
        } catch (QuerySyntaxException e) {
            log.error("Error getting projects " + e.getLocalizedMessage());
            if (isProjectNotFoundException(e)) {
                log.warn("No project founds");
                result = new ArrayList<>();
            } else {
                log.error("Error QuerySyntaxException: {}", e.getMessage());
                throw e;
            }
        }
        return result;
    }

    /**
     * Get project by selectedProjectId
     *
     * @param selectedProjectId
     * @return
     */
    @Override
    public ProjectDto getProject(Integer selectedProjectId) {
        ProjectDto result;
        log.debug("[INIT] Getting project by Id {}", selectedProjectId);
        final Project project = this.projectDao.getProject(selectedProjectId);
        if (null == project) {
            log.warn("No project found by Id {}", selectedProjectId);
            result = null;
        } else {
            log.debug("[END] Found project {}", project);
            result = this.getModelMapper().map(project, ProjectDto.class);
        }
        return result;
    }

    /**
     * Delete project
     *
     * @param project
     */
    @Override
    public void deleteProject(ProjectDto project) {
        log.debug("[INIT] deleting project " + project);
        this.projectDao.deleteProject(this.getModelMapper().map(project, Project.class));
        log.info("[END] project {} successfully deleted", project);
    }
}
