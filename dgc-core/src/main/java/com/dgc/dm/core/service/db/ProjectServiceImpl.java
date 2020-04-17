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

@Slf4j
@Service
public class ProjectServiceImpl extends CommonServer implements ProjectService {

    @Autowired
    private ProjectDao projectDao;

    @Override
    public ProjectDto createProject(final String projectName) {
        log.info("Creating project " + projectName);
        final Project projectEntity = this.projectDao.createProject(projectName);
        final ProjectDto project = this.getModelMapper().map(projectEntity, ProjectDto.class);
        log.info("Project " + project + " successfully created");
        return project;
    }
}
