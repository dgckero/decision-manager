/*
  @author david
 */

package com.dgc.dm.core.service.db;

import com.dgc.dm.core.db.dao.ProjectDao;
import com.dgc.dm.core.db.model.Project;
import com.dgc.dm.core.dto.ProjectDto;
import lombok.extern.log4j.Log4j2;
import org.hibernate.exception.GenericJDBCException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.UncategorizedSQLException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.sqlite.SQLiteErrorCode;
import org.sqlite.SQLiteException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
    private static boolean isProjectNotFoundException(Exception e) {
        return (e instanceof UncategorizedSQLException &&
                ((UncategorizedSQLException) e).getSQLException() != null &&
                ((UncategorizedSQLException) e).getSQLException().getErrorCode() == SQLiteErrorCode.SQLITE_ERROR.code)
                || (e instanceof SQLiteException && ((SQLiteException) e).getErrorCode() == SQLiteErrorCode.SQLITE_ERROR.code);
    }

    /**
     * Create project based on projectName
     *
     * @param projectName
     * @return project
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
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
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
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
    public final List<Map<String, Object>> getProjects() {
        List<Map<String, Object>> result = null;
        log.debug("[INIT] Getting projects ");
        try {
            final List<Map<String, Object>> projects = this.projectDao.getProjects();
            if (projects.isEmpty()) {
                log.warn("No project founds");
            } else {
                log.debug("[END] Got {} projects", projects.size());
                result = projects;
            }
        } catch (UncategorizedSQLException e) {
            log.error("Error getting projects " + e.getLocalizedMessage());
            if (isProjectNotFoundException(e)) {
                log.warn("No project founds");
                result = new ArrayList<>();
            } else {
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
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void deleteProject(ProjectDto project) {
        log.debug("[INIT] deleting project " + project);
        this.projectDao.deleteProject(this.getModelMapper().map(project, Project.class));
        log.info("[END] project {} successfully deleted", project);
    }
}
