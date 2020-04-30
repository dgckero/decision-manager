/*
  @author david
 */

package com.dgc.dm.core.service.db;

import com.dgc.dm.core.db.dao.ProjectDao;
import com.dgc.dm.core.db.model.Project;
import com.dgc.dm.core.dto.ProjectDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@Transactional
public class ProjectServiceImpl extends CommonServer implements ProjectService {

    @Autowired
    private ProjectDao projectDao;

    /**
     * Create project based on projectName
     *
     * @param projectName
     * @return project
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public ProjectDto createProject(String projectName) {
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
     */
    @Override
    public final List<Map<String, Object>> getProjects() {
        log.debug("[INIT] Getting projects ");
        final List<Map<String, Object>> projects = this.projectDao.getProjects();
        if (projects.isEmpty()) {
            log.warn("No project founds");
            return null;
        }
        log.debug("[END] Got {} projects", projects.size());
        return projects;
    }

    /**
     * Get project by selectedProjectId
     *
     * @param selectedProjectId
     * @return
     */
    @Override
    public ProjectDto getProject(Integer selectedProjectId) {
        log.debug("[INIT] Getting project by Id {}", selectedProjectId);
        final Project project = this.projectDao.getProject(selectedProjectId);
        if (null == project) {
            log.warn("No project found by Id {}", selectedProjectId);
            return null;
        } else {
            log.debug("[END] Found project {}", project);
            return this.getModelMapper().map(project, ProjectDto.class);
        }
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
