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
public class ProjectServiceImpl extends CommonServer implements ProjectService {

    @Autowired
    private ProjectDao projectDao;

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public ProjectDto createProject(final String projectName) {
        log.info("Creating project " + projectName);
        final Project projectEntity = this.projectDao.createProject(projectName);
        final ProjectDto project = this.getModelMapper().map(projectEntity, ProjectDto.class);
        log.info("Project " + project + " successfully created");
        return project;
    }

    @Override
    public void updateProject(ProjectDto project) {
        log.info("Updating project {}", project);
        Project projectEntity = getModelMapper().map(project, Project.class);
        projectDao.updateProject(projectEntity);
        log.info("Project {} successfully updated", project);
    }

    @Override
    public final List<Map<String, Object>> getProjects() {
        log.info("Getting projects ");
        List<Map<String, Object>> projects = projectDao.getProjects();
        if (projects.isEmpty()) {
            log.info("No project founds");
            return null;
        }
        log.info("Got {} projects", projects.size());
        return projects;
    }

    @Override
    public ProjectDto getProject(final Integer selectedProjectId) {
        log.info("Getting project by Id {}", selectedProjectId);
        Project project = projectDao.getProject(selectedProjectId);
        if (null == project) {
            log.warn("No project found by Id {}", selectedProjectId);
            return null;
        } else {
            log.info("Found project {}", project);
            return getModelMapper().map(project, ProjectDto.class);
        }
    }

    @Override
    public void deleteProject(final ProjectDto project) {
        log.info("deleting project " + project);
        projectDao.deleteProject(getModelMapper().map(project, Project.class));
        log.info("project {} successfully deleted", project);
    }
}
