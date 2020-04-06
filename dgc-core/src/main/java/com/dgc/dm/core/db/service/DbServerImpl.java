/*
  @author david
 */
package com.dgc.dm.core.db.service;

import com.dgc.dm.core.db.model.Filter;
import com.dgc.dm.core.db.model.Project;
import com.dgc.dm.core.db.repository.CommonRepository;
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
    private CommonRepository commonRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ModelMapper modelMapper;

    private String getDBClassByColumnType(String columnClassName) {

        CLAZZ cl = CLAZZ.valueOf(columnClassName.toUpperCase());

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
    public void createFilterTable(final ProjectDto project) {
        log.info(String.format("****** Creating table: %s ******", "Filters"));

        List<String> filterTableStatements = new ArrayList<String>() {
            {
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
        };

        filterTableStatements.forEach(sql -> {
            log.debug(sql);
            jdbcTemplate.execute(sql);
        });
        log.info("FILTERS table successfully created");
    }

    @Override
    public List<Filter> createCommonDatasTable(final Map<String, Class<?>> columns, final ProjectDto project) {
        log.info(String.format("****** Creating table: %s ******", "COMMONDATAS"));

        List<Filter> filterList = new ArrayList<>();
        String commonDataTableStatements = "CREATE TABLE IF NOT EXISTS COMMONDATAS (rowId INTEGER, ";

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

        log.info("COMMONDATAS table successfully created");
        return filterList;
    }

    @Override
    public void persistFilterList(List<Filter> filterList) {
        log.debug("Persisting filters got from Excel");
        filterRepository.saveAll(filterList);
        log.debug("Persisted filters got from Excel");
    }

    @Override
    @Transactional
    public void persistExcelRows(final String insertSentence, final List<Object[]> infoToBePersisted) {
        log.info("****** Persisting Excel rows into commonDatas table: %s ******");
        jdbcTemplate.batchUpdate(insertSentence, infoToBePersisted);
        log.info("****** Persisted Excel rows into commonDatas table: %s ******");
    }

    @Override
    public List<Map<String, Object>> getFilters() {
        log.info("Getting Filters");
        List<Map<String, Object>> filters = jdbcTemplate.queryForList("Select * from FILTERS");
        log.info("Got filters");
        return filters;
    }

    @Override
    public List<Map<String, Object>> getFilters(ProjectDto project) {
        log.info("Getting Filters by project " + project);
        List<Map<String, Object>> filters = jdbcTemplate.queryForList("Select * from FILTERS where project=" + project.getId());
        log.info("Got filters");
        return filters;
    }

    @Override
    public void updateFilters(List<FilterDto> activeFilters) {
        log.info("Updating filters ");

        List<Filter> filterEntityList = modelMapper.map(activeFilters, (new TypeToken<List<Filter>>() {
        }.getType()));

        log.debug("FiltersDto mapped to FiltersEntity");

        filterRepository.saveAll(filterEntityList);

        log.info("Filters updated");
    }

    @Override
    public List<Map<String, Object>> getCommonData() {
        log.info("Getting CommonData");

        List<Map<String, Object>> entities = jdbcTemplate.queryForList("Select * from COMMONDATAS");

        log.info("Got CommonData");
        return entities;
    }

    @Override
    public List<Map<String, Object>> getCommonData(ProjectDto project) {
        log.info("Getting CommonData");

        List<Map<String, Object>> entities = jdbcTemplate.queryForList("Select * from COMMONDATAS where project=" + project.getId());

        log.info("Got CommonData");
        return entities;
    }

    @Override
    public Project createProject(String projectName) {

        createProjectTable();

        log.info("Creating project " + projectName);

        Project project = projectRepository.saveAndFlush(
                Project.builder()
                        .name(projectName)
                        .createDate(new Date())
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
                        "createDate REAL NOT NULL," +
                        "emailTemplate TEXT)";

        jdbcTemplate.execute(createTableProject);

        log.debug("Table PROJECTS successfully created");
    }

    @Override
    public Filter getContactFilter(final ProjectDto project) {
        log.info("Getting Filters having contactFilter active for project " + project);
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

            log.info("Got filter " + filter);

        } catch (EmptyResultDataAccessException e) {
            log.info("No filter found having contactFilter active for project " + project);
        }
        return filter;
    }

    @Override
    public void updateProject(ProjectDto project) {
        log.info("Updating project " + project);

        Project projectEntity = modelMapper.map(project, Project.class);
        projectRepository.saveAndFlush(projectEntity);

        log.info("Project " + project + " successfully updated");
    }

    @Override
    public List<Map<String, Object>> getProjects() {
        log.info("Getting projects ");
        List<Map<String, Object>> projects = jdbcTemplate.queryForList("Select * from PROJECTS");
        if (projects == null || projects.isEmpty()) {
            log.info("No project founds");
            return null;
        }
        log.info("Got " + projects.size() + " projects");
        return projects;
    }

    @Override
    public ProjectDto getProject(final Integer selectedProjectId) {
        log.debug("Getting project by id " + selectedProjectId);

        Optional<Project> optionalProject = projectRepository.findById(selectedProjectId);

        if (optionalProject.isPresent()) {
            ProjectDto projectDto = modelMapper.map(optionalProject.get(), ProjectDto.class);
            log.debug("project found " + projectDto);
            return projectDto;
        } else {
            log.warn("No project found for id " + selectedProjectId);
            return null;
        }
    }
}
