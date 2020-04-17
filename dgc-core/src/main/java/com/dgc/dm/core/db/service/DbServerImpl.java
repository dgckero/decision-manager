/*
  @author david
 */
package com.dgc.dm.core.db.service;

import com.dgc.dm.core.db.model.Filter;
import com.dgc.dm.core.db.model.Project;
import com.dgc.dm.core.db.repository.FilterRepository;
import com.dgc.dm.core.db.repository.ProjectRepository;
import com.dgc.dm.core.dto.FilterDto;
import com.dgc.dm.core.dto.ProjectDto;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.text.SimpleDateFormat;
import java.util.*;

enum CLAZZ {
    DATE(java.util.Date.class.getSimpleName()),
    STRING(java.lang.String.class.getSimpleName()),
    DOUBLE(java.lang.Double.class.getSimpleName()),
    INTEGER(java.lang.Integer.class.getSimpleName()),
    EMAIL("Email");

    private final String simpleNameClass;

    CLAZZ(final String simpleNameClass) {
        this.simpleNameClass = simpleNameClass;
    }

    String getSimpleNameClass() {
        return this.simpleNameClass;
    }
}

@Slf4j
@Service
public class DbServerImpl implements DbServer {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private FilterRepository filterRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ModelMapper modelMapper;

    private static String getDBClassByColumnType(final String columnClassName) {

        final CLAZZ cl = CLAZZ.valueOf(columnClassName.toUpperCase());

        switch (cl) {
            case INTEGER:
                return "INTEGER";
            case DATE:
            case DOUBLE:
                return "REAL";
            case STRING:
            case EMAIL:
            default:
                return "TEXT";
        }
    }

    @Override
    public final void createFilterTable(ProjectDto project) {
        log.info(String.format("****** Creating table: %s ******", "Filters"));

        final List<String> filterTableStatements = new StringArrayList(project);

        filterTableStatements.forEach(sql -> {
            log.debug(sql);
            this.jdbcTemplate.execute(sql);
        });
        log.info("FILTERS table successfully created");
    }

    @Override
    public final void persistFilterList(final List<Filter> filterList) {
        log.debug("Persisting filters got from Excel");
        this.filterRepository.saveAll(filterList);
        log.debug("Persisted filters got from Excel");
    }

    private void deleteFilters(ProjectDto project) {
        log.debug("deleting Filters for project {}", project);
        this.jdbcTemplate.execute("DELETE FROM FILTERS where project=" + project.getId());
        log.debug("Filters successfully deleted for project {}", project);
    }

    @Override
    public final List<Map<String, Object>> getFilters() {
        log.info("Getting Filters");
        final List<Map<String, Object>> filters = this.jdbcTemplate.queryForList("Select * from FILTERS");
        log.info("Got filters");
        return filters;
    }

    @Override
    public final List<Map<String, Object>> getFilters(final ProjectDto project) {
        log.info("Getting Filters by project {}", project);
        final List<Map<String, Object>> filters = this.jdbcTemplate.queryForList("Select * from FILTERS where project=" + project.getId());
        log.info("Got filters");
        return filters;
    }

    @Override
    public final void updateFilters(final List<FilterDto> activeFilters) {
        log.info("Updating filters ");
        final List<Filter> filterEntityList = this.modelMapper.map(activeFilters, (new ListTypeToken().getType()));
        log.debug("FiltersDto mapped to FiltersEntity");
        this.filterRepository.saveAll(filterEntityList);
        log.info("Filters updated");
    }

    @Override
    public final List<Filter> createCommonDatasTable(Map<String, Class<?>> columns, ProjectDto project) {
        log.info(String.format("****** Creating table: %s ******", project.getCommonDataTableName()));

        final List<Filter> filterList = new ArrayList<>();
        String commonDataTableStatements = "CREATE TABLE IF NOT EXISTS " + project.getCommonDataTableName() + " (rowId INTEGER, ";

        for (final Map.Entry<String, Class<?>> column : columns.entrySet()) {
            filterList.add(Filter.builder().
                    name(column.getKey()).
                    filterClass(column.getValue().getSimpleName()).
                    active(Boolean.FALSE).
                    contactFilter(Boolean.FALSE).
                    project(this.modelMapper.map(project, Project.class)).
                    build());

            commonDataTableStatements += column.getKey() + " " + getDBClassByColumnType(column.getValue().getSimpleName()) + ",";
        }

        final String foreignKey = ", project INTEGER NOT NULL,FOREIGN KEY(project) REFERENCES PROJECTS(id), " +
                "PRIMARY KEY (rowId, project) )";

        commonDataTableStatements = commonDataTableStatements.replaceAll("[,]$", foreignKey);
        this.jdbcTemplate.execute(commonDataTableStatements);

        log.info("{} table successfully created", project.getCommonDataTableName());
        return filterList;
    }

    @Override
    @Transactional
    public final void persistExcelRows(String insertSentence, List<Object[]> infoToBePersisted) {
        log.info("****** Persisting Excel rows into commonDatas table: %s ******");
        this.jdbcTemplate.batchUpdate(insertSentence, infoToBePersisted);
        log.info("****** Persisted Excel rows into commonDatas table: %s ******");
    }

    @Override
    public final List<Map<String, Object>> getCommonData(final ProjectDto project) {
        log.info("Getting all info from table: {}", project.getCommonDataTableName());

        final List<Map<String, Object>> entities = this.jdbcTemplate.queryForList("Select * from " + project.getCommonDataTableName() + " where project=" + project.getId());

        log.info("Got all info from table: {}", project.getCommonDataTableName());
        return entities;
    }

    @Override
    public final Project createProject(final String projectName) {

        this.createProjectTable();

        log.info("Creating project {}", projectName);

        final Project project = this.projectRepository.saveAndFlush(
                Project.builder()
                        .name(projectName)
                        .commonDataTableName("COMMONDATAS_" + projectName)
                        .createDate(new SimpleDateFormat("yyyy-MM-dd").format(new Date()))
                        .build()
        );

        log.info("Project {} successfully created", project);

        return project;

    }

    private void createProjectTable() {
        log.debug("Creating table PROJECTS if not exist");

        final String createTableProject =
                "CREATE TABLE IF NOT EXISTS PROJECTS " +
                        "(ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "name TEXT NOT NULL," +
                        "commonDataTableName TEXT NOT NULL," +
                        "createDate TEXT NOT NULL," +
                        "emailTemplate TEXT," +
                        "dmnFile BLOB)";

        this.jdbcTemplate.execute(createTableProject);

        log.debug("Table PROJECTS successfully created");
    }

    @Override
    public final Filter getContactFilter(ProjectDto project) {
        log.info("Getting Filters having contactFilter active for project {}", project);
        Filter filter = null;
        final String sql = "Select * from FILTERS where contactFilter=? and project=?";

        try {
            filter = this.jdbcTemplate.queryForObject(sql, new Object[]{1, project.getId()}, (rs, rowNum) ->
                    new Filter(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getString("class"),
                            rs.getString("value"),
                            rs.getBoolean("active"),
                            rs.getBoolean("contactFilter"),
                            Project.builder().id(rs.getInt("project")).build()
                    ));

            log.info("Got filter {}", filter);

        } catch (final EmptyResultDataAccessException e) {
            log.info("No filter found having contactFilter active for project {}", project);
        }
        return filter;
    }

    @Override
    public final void updateProject(final ProjectDto project) {
        log.info("Updating project {}", project);

        final Project projectEntity = this.modelMapper.map(project, Project.class);
        this.projectRepository.saveAndFlush(projectEntity);

        log.info("Project {} successfully updated", project);
    }

    @Override
    public final List<Map<String, Object>> getProjects() {
        log.info("Getting projects ");
        final List<Map<String, Object>> projects = this.jdbcTemplate.queryForList("Select * from PROJECTS");
        if (projects.isEmpty()) {
            log.info("No project founds");
            return null;
        }
        log.info("Got {} projects", projects.size());
        return projects;
    }

    @Override
    public final ProjectDto getProject(Integer selectedProjectId) {
        log.debug("Getting project by id {}", selectedProjectId);

        final Optional<Project> optionalProject = this.projectRepository.findById(selectedProjectId);

        if (optionalProject.isPresent()) {
            final ProjectDto projectDto = this.modelMapper.map(optionalProject.get(), ProjectDto.class);
            log.debug("project found {}", projectDto);
            return projectDto;
        } else {
            log.warn("No project found for id {}", selectedProjectId);
            return null;
        }
    }

    @Override
    public final void deleteCommonData(ProjectDto project) {
        log.debug("Deleting all registers for project {}", project);
        this.jdbcTemplate.execute("DELETE FROM " + project.getCommonDataTableName());
        log.debug("Registers successfully deleted for project {}", project);
    }

    @Override
    @Transactional
    public final void deleteProject(ProjectDto project) {
        log.debug("deleting project {}", project);
        deleteCommonData(project);
        deleteFilters(project);
        this.jdbcTemplate.execute("DELETE FROM PROJECTS where id=" + project.getId());
        log.debug("project successfully deleted");
    }

    private static class ListTypeToken extends TypeToken<List<Filter>> {
    }

    private static class StringArrayList extends ArrayList<String> {
        public StringArrayList(final ProjectDto project) {
            this.add("CREATE TABLE IF NOT EXISTS FILTERS " +
                    "( ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "name TEXT," +
                    "class TEXT, " +
                    "active INTEGER default 0," +
                    "contactFilter INTEGER default 0," +
                    "value TEXT default NULL," +
                    "project INTEGER NOT NULL," +
                    "FOREIGN KEY(project) REFERENCES PROJECTS(id)," +
                    "CONSTRAINT UQ_NAME_PROJ UNIQUE (name, project) )");
            this.add("INSERT INTO FILTERS (name, class, project) values ('rowId','java.lang.Integer','" + project.getId() + "')");
        }
    }
}
