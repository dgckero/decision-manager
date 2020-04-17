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

    CLAZZ(String simpleNameClass) {
        this.simpleNameClass = simpleNameClass;
    }

    String getSimpleNameClass() {
        return simpleNameClass;
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

    private static String getDBClassByColumnType(String columnClassName) {

        CLAZZ cl = CLAZZ.valueOf(columnClassName.toUpperCase());

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
    public final void createFilterTable(final ProjectDto project) {
        log.info(String.format("****** Creating table: %s ******", "Filters"));

        List<String> filterTableStatements = new StringArrayList(project);

        filterTableStatements.forEach(sql -> {
            log.debug(sql);
            jdbcTemplate.execute(sql);
        });
        log.info("FILTERS table successfully created");
    }

    @Override
    public final void persistFilterList(List<Filter> filterList) {
        log.debug("Persisting filters got from Excel");
        filterRepository.saveAll(filterList);
        log.debug("Persisted filters got from Excel");
    }

    private void deleteFilters(final ProjectDto project) {
        log.debug("deleting Filters for project {}", project);
        jdbcTemplate.execute("DELETE FROM FILTERS where project=" + project.getId());
        log.debug("Filters successfully deleted for project {}", project);
    }

    @Override
    public final List<Map<String, Object>> getFilters() {
        log.info("Getting Filters");
        List<Map<String, Object>> filters = jdbcTemplate.queryForList("Select * from FILTERS");
        log.info("Got filters");
        return filters;
    }

    @Override
    public final List<Map<String, Object>> getFilters(ProjectDto project) {
        log.info("Getting Filters by project {}", project);
        List<Map<String, Object>> filters = jdbcTemplate.queryForList("Select * from FILTERS where project=" + project.getId());
        log.info("Got filters");
        return filters;
    }

    @Override
    public final void updateFilters(List<FilterDto> activeFilters) {
        log.info("Updating filters ");
        List<Filter> filterEntityList = modelMapper.map(activeFilters, (new ListTypeToken().getType()));
        log.debug("FiltersDto mapped to FiltersEntity");
        filterRepository.saveAll(filterEntityList);
        log.info("Filters updated");
    }

    @Override
    public final List<Filter> createCommonDatasTable(final Map<String, Class<?>> columns, final ProjectDto project) {
        log.info(String.format("****** Creating table: %s ******", project.getCommonDataTableName()));

        List<Filter> filterList = new ArrayList<>();
        String commonDataTableStatements = "CREATE TABLE IF NOT EXISTS " + project.getCommonDataTableName() + " (rowId INTEGER, ";

        for (Map.Entry<String, Class<?>> column : columns.entrySet()) {
            filterList.add(Filter.builder().
                    name(column.getKey()).
                    filterClass(column.getValue().getSimpleName()).
                    active(Boolean.FALSE).
                    contactFilter(Boolean.FALSE).
                    project(modelMapper.map(project, Project.class)).
                    build());

            commonDataTableStatements += column.getKey() + " " + getDBClassByColumnType(column.getValue().getSimpleName()) + ",";
        }

        final String foreignKey = ", project INTEGER NOT NULL,FOREIGN KEY(project) REFERENCES PROJECTS(id), " +
                "PRIMARY KEY (rowId, project) )";

        commonDataTableStatements = commonDataTableStatements.replaceAll("[,]$", foreignKey);
        jdbcTemplate.execute(commonDataTableStatements);

        log.info("{} table successfully created", project.getCommonDataTableName());
        return filterList;
    }

    @Override
    @Transactional
    public final void persistExcelRows(final String insertSentence, final List<Object[]> infoToBePersisted) {
        log.info("****** Persisting Excel rows into commonDatas table: %s ******");
        jdbcTemplate.batchUpdate(insertSentence, infoToBePersisted);
        log.info("****** Persisted Excel rows into commonDatas table: %s ******");
    }

    @Override
    public final List<Map<String, Object>> getCommonData(ProjectDto project) {
        log.info("Getting all info from table: {}", project.getCommonDataTableName());

        List<Map<String, Object>> entities = jdbcTemplate.queryForList("Select * from " + project.getCommonDataTableName() + " where project=" + project.getId());

        log.info("Got all info from table: {}", project.getCommonDataTableName());
        return entities;
    }

    @Override
    public final Project createProject(String projectName) {

        createProjectTable();

        log.info("Creating project {}", projectName);

        Project project = projectRepository.saveAndFlush(
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

        jdbcTemplate.execute(createTableProject);

        log.debug("Table PROJECTS successfully created");
    }

    @Override
    public final Filter getContactFilter(final ProjectDto project) {
        log.info("Getting Filters having contactFilter active for project {}", project);
        Filter filter = null;
        final String sql = "Select * from FILTERS where contactFilter=? and project=?";

        try {
            filter = jdbcTemplate.queryForObject(sql, new Object[]{1, project.getId()}, (rs, rowNum) ->
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

        } catch (EmptyResultDataAccessException e) {
            log.info("No filter found having contactFilter active for project {}", project);
        }
        return filter;
    }

    @Override
    public final void updateProject(ProjectDto project) {
        log.info("Updating project {}", project);

        Project projectEntity = modelMapper.map(project, Project.class);
        projectRepository.saveAndFlush(projectEntity);

        log.info("Project {} successfully updated", project);
    }

    @Override
    public final List<Map<String, Object>> getProjects() {
        log.info("Getting projects ");
        List<Map<String, Object>> projects = jdbcTemplate.queryForList("Select * from PROJECTS");
        if (projects.isEmpty()) {
            log.info("No project founds");
            return null;
        }
        log.info("Got {} projects", projects.size());
        return projects;
    }

    @Override
    public final ProjectDto getProject(final Integer selectedProjectId) {
        log.debug("Getting project by id {}", selectedProjectId);

        Optional<Project> optionalProject = projectRepository.findById(selectedProjectId);

        if (optionalProject.isPresent()) {
            ProjectDto projectDto = modelMapper.map(optionalProject.get(), ProjectDto.class);
            log.debug("project found {}", projectDto);
            return projectDto;
        } else {
            log.warn("No project found for id {}", selectedProjectId);
            return null;
        }
    }

    @Override
    public final void deleteCommonData(final ProjectDto project) {
        log.debug("Deleting all registers for project {}", project);
        jdbcTemplate.execute("DELETE FROM " + project.getCommonDataTableName());
        log.debug("Registers successfully deleted for project {}", project);
    }

    @Override
    @Transactional
    public final void deleteProject(final ProjectDto project) {
        log.debug("deleting project {}", project);
        this.deleteCommonData(project);
        this.deleteFilters(project);
        jdbcTemplate.execute("DELETE FROM PROJECTS where id=" + project.getId());
        log.debug("project successfully deleted");
    }

    private static class ListTypeToken extends TypeToken<List<Filter>> {
    }

    private static class StringArrayList extends ArrayList<String> {
        public StringArrayList(ProjectDto project) {
            add("CREATE TABLE IF NOT EXISTS FILTERS " +
                    "( ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "name TEXT," +
                    "class TEXT, " +
                    "active INTEGER default 0," +
                    "contactFilter INTEGER default 0," +
                    "value TEXT default NULL," +
                    "project INTEGER NOT NULL," +
                    "FOREIGN KEY(project) REFERENCES PROJECTS(id)," +
                    "CONSTRAINT UQ_NAME_PROJ UNIQUE (name, project) )");
            add("INSERT INTO FILTERS (name, class, project) values ('rowId','java.lang.Integer','" + project.getId() + "')");
        }
    }
}
