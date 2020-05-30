/*
  @author david
 */

package com.dgc.dm.core.db.dao;

import com.dgc.dm.core.db.model.Project;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Log4j2
@Service
public class ProjectDaoImpl extends CommonDao implements ProjectDao {

    private static final String COMMONDATAS_PREFIX_TABLE_NAME = "COMMONDATAS_";

    /**
     * Create table Projects if not exist
     */
    public void createProjectTable() {
        log.debug("[INIT] Creating table PROJECTS if not exist");
        final String createTableProject =
                "CREATE TABLE IF NOT EXISTS PROJECTS " +
                        "(ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "name TEXT UNIQUE NOT NULL," +
                        "rowDataTableName TEXT NOT NULL," +
                        "dataCreationDate TEXT NOT NULL," +
                        "lastUpdatedDate TEXT," +
                        "emailTemplate TEXT," +
                        "dmnFile BLOB)";

        sessionFactory.getCurrentSession().createNativeQuery(createTableProject).executeUpdate();
        log.debug("[END] Table PROJECTS successfully created");
    }

    /**
     * Create project with name projectName
     *
     * @param projectName name of new project
     * @return Project created
     */
    @Override
    public Project createProject(String projectName) {
        log.debug("[INIT] Creating project " + projectName);
        this.createProjectTable();
        Project project =
                Project.builder()
                        .name(projectName)
                        .rowDataTableName(COMMONDATAS_PREFIX_TABLE_NAME + projectName)
                        .dataCreationDate(new SimpleDateFormat("yyyy-MM-dd HH24:mm:ss").format(new Date()))
                        .build();
        sessionFactory.getCurrentSession().persist(project);
        log.debug("[END] Project " + project + " successfully created");
        return project;
    }

    /**
     * Update project entity
     *
     * @param project
     */
    @Override
    public void updateProject(Project project) {
        log.debug("[INIT] Updating project " + project);
        sessionFactory.getCurrentSession().merge(project);
        log.debug("[END] Project " + project + " successfully updated");
    }

    /**
     * Get all projects
     *
     * @return all projects
     */
    @Override
    public List<Project> getProjects() {
        List<Project> result = null;
        log.debug("[INIT] Getting projects ");
        result = this.sessionFactory.getCurrentSession().createQuery("from Project").list();
        if (result.isEmpty()) {
            log.warn("[END] No project founds");
        } else {
            log.debug("[INIT] Got " + result.size() + " projects");
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
    public Project getProject(Integer selectedProjectId) {
        Project result;
        log.debug("[INIT] Getting project by id " + selectedProjectId);

        result = sessionFactory.getCurrentSession().get(Project.class, selectedProjectId);
        log.debug("[END] project found " + result);
        return result;
    }

    /**
     * Delete project
     *
     * @param project
     */
    @Override
    public void deleteProject(Project project) {
        log.debug("[INIT] deleting project " + project);
        sessionFactory.getCurrentSession().delete(project);
        log.debug("[END] project successfully deleted");
    }
}
