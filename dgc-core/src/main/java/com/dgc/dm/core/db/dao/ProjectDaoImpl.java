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
    private void createProjectTable ( ) {
        log.debug("[INIT] Creating table PROJECTS if not exist");
        final String createTableProject =
                "CREATE TABLE IF NOT EXISTS PROJECTS " +
                        "(ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "name TEXT NOT NULL," +
                        "rowDataTableName TEXT NOT NULL," +
                        "createDate TEXT NOT NULL," +
                        "emailTemplate TEXT," +
                        "dmnFile BLOB)";

        this.sessionFactory.getCurrentSession().createSQLQuery(createTableProject).executeUpdate();
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
    public Project createProject (final String projectName) {
        log.debug("[INIT] Creating project " + projectName);
        createProjectTable();
        final Project project =
                Project.builder()
                        .name(projectName)
                        .rowDataTableName("COMMONDATAS_" + projectName)
                        .createDate(new SimpleDateFormat("yyyy-MM-dd").format(new Date()))
                        .build();
        this.sessionFactory.getCurrentSession().persist(project);
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
    public void updateProject (final Project project) {
        log.debug("[INIT] Updating project " + project);
        this.sessionFactory.getCurrentSession().merge(project);
        log.debug("[END] Project " + project + " successfully updated");
    }

    /**
     * Get all projects
     *
     * @return all projects
     */
    @Override
    public List<Map<String, Object>> getProjects ( ) {
        List<Map<String, Object>> result = null;
        log.debug("[INIT] Getting projects ");
        List<Map<String, Object>> projects = this.getJdbcTemplate().queryForList("Select * from PROJECTS");
        if (projects == null || projects.isEmpty()) {
            log.warn("[END] No project founds");
        } else {
            log.debug("[INIT] Got " + projects.size() + " projects");
            result = projects;
        }
        return result;
    }

    /**
     * Get project by selectedProjectId
     *
     * @param selectedProjectId
     * @return project with id = selectedProjectId
     */
    @Override
    public Project getProject (final Integer selectedProjectId) {
        final Project result;
        log.debug("[INIT] Getting project by id " + selectedProjectId);

        result = this.sessionFactory.getCurrentSession().find(Project.class, selectedProjectId);
        log.debug("[END] project found " + result);
        return result;
    }

    /**
     * Delete project
     *
     * @param project
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void deleteProject (final Project project) {
        log.debug("[INIT] deleting project " + project);
        this.sessionFactory.getCurrentSession().delete(project);
        log.debug("[END] project successfully deleted");
    }
}
