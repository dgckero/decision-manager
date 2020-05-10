/*
  @author david
 */

package com.dgc.dm.core.service.db;

import com.dgc.dm.core.db.dao.ProjectDao;
import com.dgc.dm.core.db.model.Project;
import com.dgc.dm.core.dto.ProjectDto;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
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
    private static boolean isProjectNotFoundException (final Exception e) {
        return (e instanceof UncategorizedSQLException && ((UncategorizedSQLException) e).getSQLException().getErrorCode() == SQLiteErrorCode.SQLITE_ERROR.code)
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
    public ProjectDto createProject (final String projectName) throws GenericJDBCException {
        log.debug("[INIT] createProject " + projectName);
        final Project projectEntity = this.projectDao.createProject(projectName);
        final ProjectDto project = this.getModelMapper().map(projectEntity, ProjectDto.class);
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
    public void updateProject (ProjectDto project) {
        log.debug("[INIT] Updating project {}", project);
        Project projectEntity = getModelMapper().map(project, Project.class);
        projectDao.updateProject(projectEntity);
        log.debug("[END] Project {} successfully updated", project);
    }

    /**
     * Get all projects
     *
     * @return list of projects
     * @throws SQLiteException
     */
    @Override
    public final List<Map<String, Object>> getProjects ( ) throws SQLiteException {
        List<Map<String, Object>> result = null;
        log.debug("[INIT] Getting projects ");
        try {
            List<Map<String, Object>> projects = projectDao.getProjects();
            if (projects.isEmpty()) {
                log.warn("No project founds");
            } else {
                log.debug("[END] Got {} projects", projects.size());
                result = projects;
            }
        } catch (final UncategorizedSQLException | SQLiteException e) {
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
    public ProjectDto getProject (final Integer selectedProjectId) {
        final ProjectDto result;
        log.debug("[INIT] Getting project by Id {}", selectedProjectId);
        Project project = projectDao.getProject(selectedProjectId);
        if (null == project) {
            log.warn("No project found by Id {}", selectedProjectId);
            result = null;
        } else {
            log.debug("[END] Found project {}", project);
            result = getModelMapper().map(project, ProjectDto.class);
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
    public void deleteProject (final ProjectDto project) {
        log.debug("[INIT] deleting project " + project);
        projectDao.deleteProject(getModelMapper().map(project, Project.class));
        log.info("[END] project {} successfully deleted", project);
    }
}
