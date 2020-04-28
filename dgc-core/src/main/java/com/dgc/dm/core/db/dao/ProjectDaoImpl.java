/*
  @author david
 */

package com.dgc.dm.core.db.dao;

import com.dgc.dm.core.db.model.Project;
import com.dgc.dm.core.db.repository.ProjectRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
public class ProjectDaoImpl extends CommonDao implements ProjectDao {

    @Autowired
    private ProjectRepository projectRepository;

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    private void createProjectTable() {
        log.debug("Creating table PROJECTS if not exist");
        final String createTableProject =
                "CREATE TABLE IF NOT EXISTS PROJECTS " +
                        "(ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "name TEXT NOT NULL," +
                        "rowDataTableName TEXT NOT NULL," +
                        "createDate TEXT NOT NULL," +
                        "emailTemplate TEXT," +
                        "dmnFile BLOB)";

        this.getJdbcTemplate().execute(createTableProject);
        log.debug("Table PROJECTS successfully created");
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public Project createProject(final String projectName) {
        log.info("Creating project " + projectName);
        createProjectTable();
        Project project = projectRepository.saveAndFlush(
                Project.builder()
                        .name(projectName)
                        .rowDataTableName("COMMONDATAS_" + projectName)
                        .createDate(new SimpleDateFormat("yyyy-MM-dd").format(new Date()))
                        .build()
        );
        log.info("Project " + project + " successfully created");
        return project;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void updateProject(final Project project) {
        log.info("Updating project " + project);
        projectRepository.saveAndFlush(project);
        log.info("Project " + project + " successfully updated");
    }

    @Override
    public List<Map<String, Object>> getProjects() {
        log.info("Getting projects ");
        List<Map<String, Object>> projects = this.getJdbcTemplate().queryForList("Select * from PROJECTS");
        if (projects == null || projects.isEmpty()) {
            log.info("No project founds");
            return null;
        }
        log.info("Got " + projects.size() + " projects");
        return projects;
    }

    @Override
    public Project getProject(final Integer selectedProjectId) {
        log.debug("Getting project by id " + selectedProjectId);
        Optional<Project> optionalProject = projectRepository.findById(selectedProjectId);
        if (optionalProject.isPresent()) {
            log.debug("project found " + optionalProject.get());
            return optionalProject.get();
        } else {
            log.warn("No project found for id " + selectedProjectId);
            return null;
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void deleteProject(final Project project) {
        log.debug("deleting project " + project);
        this.getJdbcTemplate().execute("DELETE FROM PROJECTS where id=" + project.getId());
        log.debug("project successfully deleted");
    }
}
