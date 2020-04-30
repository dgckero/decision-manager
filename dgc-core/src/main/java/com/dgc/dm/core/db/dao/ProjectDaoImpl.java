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
@Transactional
public class ProjectDaoImpl extends CommonDao implements ProjectDao {

    @Autowired
    private ProjectRepository projectRepository;

    /**
     * Create table Projects if not exist
     */
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    private void createProjectTable() {
        log.debug("[INIT] Creating table PROJECTS if not exist");
        final String createTableProject =
                "CREATE TABLE IF NOT EXISTS PROJECTS " +
                        "(ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "name TEXT NOT NULL," +
                        "rowDataTableName TEXT NOT NULL," +
                        "createDate TEXT NOT NULL," +
                        "emailTemplate TEXT," +
                        "dmnFile BLOB)";

        getJdbcTemplate().execute(createTableProject);
        log.debug("[END] Table PROJECTS successfully created");
    }

    /**
     * Create project with name projectName
     *
     * @param projectName name of new project
     * @return Project created
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public Project createProject(String projectName) {
        log.debug("[INIT] Creating project " + projectName);
        this.createProjectTable();
        final Project project = this.projectRepository.saveAndFlush(
                Project.builder()
                        .name(projectName)
                        .rowDataTableName("COMMONDATAS_" + projectName)
                        .createDate(new SimpleDateFormat("yyyy-MM-dd").format(new Date()))
                        .build()
        );
        log.debug("[END] Project " + project + " successfully created");
        return project;
    }

    /**
     * Update project entity
     *
     * @param project
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void updateProject(Project project) {
        log.debug("[INIT] Updating project " + project);
        this.projectRepository.saveAndFlush(project);
        log.debug("[END] Project " + project + " successfully updated");
    }

    /**
     * Get all projects
     *
     * @return all projects
     */
    @Override
    public List<Map<String, Object>> getProjects() {
        log.debug("[INIT] Getting projects ");
        final List<Map<String, Object>> projects = getJdbcTemplate().queryForList("Select * from PROJECTS");
        if (projects == null || projects.isEmpty()) {
            log.warn("[END] No project founds");
            return null;
        }
        log.debug("[INIT] Got " + projects.size() + " projects");
        return projects;
    }

    /**
     * Get project by selectedProjectId
     *
     * @param selectedProjectId
     * @return project with id = selectedProjectId
     */
    @Override
    public Project getProject(Integer selectedProjectId) {
        log.debug("[INIT] Getting project by id " + selectedProjectId);
        final Optional<Project> optionalProject = this.projectRepository.findById(selectedProjectId);
        if (optionalProject.isPresent()) {
            log.debug("[END] project found " + optionalProject.get());
            return optionalProject.get();
        } else {
            log.warn("[END] No project found for id " + selectedProjectId);
            return null;
        }
    }

    /**
     * Delete project
     *
     * @param project
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void deleteProject(Project project) {
        log.debug("[INIT] deleting project " + project);
        getJdbcTemplate().execute("DELETE FROM PROJECTS where id=" + project.getId());
        log.debug("[END] project successfully deleted");
    }
}
