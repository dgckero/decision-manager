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

    private String getDBClassByColumnType(final String columnClassName) {

        final CLAZZ cl = CLAZZ.valueOf(columnClassName.toUpperCase());

        switch (cl) {
            case STRING:
            case EMAIL:
                return "TEXT";
            case INTEGER:
                return "INTEGER";
            case DATE:
            case DOUBLE:
                return "REAL";
            default:
                return "TEXT";
        }
    }

    @Override
    public void createFilterTable(ProjectDto project) {
        log.info(String.format("****** Creating table: %s ******", "Filters"));

        final List<String> filterTableStatements = new ArrayList<String>() {
            {
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
        };

        filterTableStatements.forEach(sql -> {
            log.debug(sql);
            this.jdbcTemplate.execute(sql);
        });
        log.info("FILTERS table successfully created");
    }

    @Override
    public List<Filter> createCommonDatasTable(Map<String, Class<?>> columns, ProjectDto project) {
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

            commonDataTableStatements += column.getKey() + " " + this.getDBClassByColumnType(column.getValue().getSimpleName()) + ",";
        }

        final String foreignKey = ", project INTEGER NOT NULL,FOREIGN KEY(project) REFERENCES PROJECTS(id), " +
                "PRIMARY KEY (rowId, project) )";

        commonDataTableStatements = commonDataTableStatements.replaceAll("[,]$", foreignKey);
        this.jdbcTemplate.execute(commonDataTableStatements);

        log.info(project.getCommonDataTableName() + " table successfully created");
        return filterList;
    }

    @Override
    public void persistFilterList(final List<Filter> filterList) {
        log.debug("Persisting filters got from Excel");
        this.filterRepository.saveAll(filterList);
        log.debug("Persisted filters got from Excel");
    }

    @Override
    @Transactional
    public void persistExcelRows(String insertSentence, List<Object[]> infoToBePersisted) {
        log.info("****** Persisting Excel rows into commonDatas table: %s ******");
        this.jdbcTemplate.batchUpdate(insertSentence, infoToBePersisted);
        log.info("****** Persisted Excel rows into commonDatas table: %s ******");
    }

    @Override
    public List<Map<String, Object>> getFilters() {
        log.info("Getting Filters");
        final List<Map<String, Object>> filters = this.jdbcTemplate.queryForList("Select * from FILTERS");
        log.info("Got filters");
        return filters;
    }

    @Override
    public List<Map<String, Object>> getFilters(final ProjectDto project) {
        log.info("Getting Filters by project " + project);
        final List<Map<String, Object>> filters = this.jdbcTemplate.queryForList("Select * from FILTERS where project=" + project.getId());
        log.info("Got filters");
        return filters;
    }

    @Override
    public void updateFilters(final List<FilterDto> activeFilters) {
        log.info("Updating filters ");

        final List<Filter> filterEntityList = this.modelMapper.map(activeFilters, (new TypeToken<List<Filter>>() {
        }.getType()));

        log.debug("FiltersDto mapped to FiltersEntity");

        this.filterRepository.saveAll(filterEntityList);

        log.info("Filters updated");
    }

    @Override
    public List<Map<String, Object>> getCommonData(final ProjectDto project) {
        log.info("Getting all info from table: " + project.getCommonDataTableName());

        final List<Map<String, Object>> entities = this.jdbcTemplate.queryForList("Select * from " + project.getCommonDataTableName() + " where project=" + project.getId());

        log.info("Got all info from table: " + project.getCommonDataTableName());
        return entities;
    }

    @Override
    public Project createProject(final String projectName) {

        this.createProjectTable();

        log.info("Creating project " + projectName);

        final Project project = this.projectRepository.saveAndFlush(
                Project.builder()
                        .name(projectName)
                        .commonDataTableName("COMMONDATAS_" + projectName)
                        .createDate(new SimpleDateFormat("yyyy-MM-dd").format(new Date()))
                        .build()
        );

        log.info("Project " + project + " successfully created");

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
    public Filter getContactFilter(ProjectDto project) {
        log.info("Getting Filters having contactFilter active for project " + project);
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

            log.info("Got filter " + filter);

        } catch (final EmptyResultDataAccessException e) {
            log.info("No filter found having contactFilter active for project " + project);
        }
        return filter;
    }

    @Override
    public void updateProject(final ProjectDto project) {
        log.info("Updating project " + project);

        final Project projectEntity = this.modelMapper.map(project, Project.class);
        this.projectRepository.saveAndFlush(projectEntity);

        log.info("Project " + project + " successfully updated");
    }

    @Override
    public List<Map<String, Object>> getProjects() {
        log.info("Getting projects ");
        final List<Map<String, Object>> projects = this.jdbcTemplate.queryForList("Select * from PROJECTS");
        if (projects == null || projects.isEmpty()) {
            log.info("No project founds");
            return null;
        }
        log.info("Got " + projects.size() + " projects");
        return projects;
    }

    @Override
    public ProjectDto getProject(Integer selectedProjectId) {
        log.debug("Getting project by id " + selectedProjectId);

        final Optional<Project> optionalProject = this.projectRepository.findById(selectedProjectId);

        if (optionalProject.isPresent()) {
            final ProjectDto projectDto = this.modelMapper.map(optionalProject.get(), ProjectDto.class);
            log.debug("project found " + projectDto);
            return projectDto;
        } else {
            log.warn("No project found for id " + selectedProjectId);
            return null;
        }
    }
}
