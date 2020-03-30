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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

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
    private CommonRepository commonRepository;

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
        DbServerImpl.log.info(String.format("****** Creating table: %s ******", "Filters"));

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
            DbServerImpl.log.debug(sql);
            this.jdbcTemplate.execute(sql);
        });
        DbServerImpl.log.info("FILTERS table successfully created");
    }

    @Override
    public List<Filter> createCommonDatasTable(Map<String, Class<?>> columns, ProjectDto project) {
        DbServerImpl.log.info(String.format("****** Creating table: %s ******", "COMMONDATAS"));

        final List<Filter> filterList = new ArrayList<>();
        String commonDataTableStatements = "CREATE TABLE IF NOT EXISTS COMMONDATAS (rowId INTEGER, ";

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

        DbServerImpl.log.info("COMMONDATAS table successfully created");
        return filterList;
    }

    @Override
    public void persistFilterList(final List<Filter> filterList) {
        DbServerImpl.log.debug("Persisting filters got from Excel");
        this.filterRepository.saveAll(filterList);
        DbServerImpl.log.debug("Persisted filters got from Excel");
    }

    @Transactional
    public void persistExcelRows(String insertSentence, List<Object[]> infoToBePersisted) {
        DbServerImpl.log.info("****** Persisting Excel rows into commonDatas table: %s ******");
        this.jdbcTemplate.batchUpdate(insertSentence, infoToBePersisted);
        DbServerImpl.log.info("****** Persisted Excel rows into commonDatas table: %s ******");
    }

    public List<Map<String, Object>> getFilters() {
        DbServerImpl.log.info("Getting Filters");
        final List<Map<String, Object>> filters = this.jdbcTemplate.queryForList("Select * from FILTERS");
        DbServerImpl.log.info("Got filters");
        return filters;
    }

    public List<Map<String, Object>> getFilters(final ProjectDto project) {
        DbServerImpl.log.info("Getting Filters by project " + project);
        final List<Map<String, Object>> filters = this.jdbcTemplate.queryForList("Select * from FILTERS where project=" + project.getId());
        DbServerImpl.log.info("Got filters");
        return filters;
    }

    @Override
    public void updateFilters(final List<FilterDto> activeFilters) {
        DbServerImpl.log.info("Updating filters ");

        final List<Filter> filterEntityList = this.modelMapper.map(activeFilters, (new TypeToken<List<Filter>>() {
        }.getType()));

        DbServerImpl.log.debug("FiltersDto mapped to FiltersEntity");

        this.filterRepository.saveAll(filterEntityList);

        DbServerImpl.log.info("Filters updated");
    }

    @Override
    public List<Map<String, Object>> getCommonData() {
        DbServerImpl.log.info("Getting CommonData");

        final List<Map<String, Object>> entities = this.jdbcTemplate.queryForList("Select * from COMMONDATAS");

        DbServerImpl.log.info("Got CommonData");
        return entities;
    }

    @Override
    public List<Map<String, Object>> getCommonData(final ProjectDto project) {
        DbServerImpl.log.info("Getting CommonData");

        final List<Map<String, Object>> entities = this.jdbcTemplate.queryForList("Select * from COMMONDATAS where project=" + project.getId());

        DbServerImpl.log.info("Got CommonData");
        return entities;
    }

    @Override
    public Project createProject(final String projectName) {

        this.createProjectTable();

        DbServerImpl.log.info("Creating project " + projectName);

        final Project project = this.projectRepository.saveAndFlush(
                Project.builder()
                        .name(projectName)
                        .createDate(new Date())
                        .build()
        );

        DbServerImpl.log.info("Project " + project + " successfully created");

        return project;

    }

    private void createProjectTable() {
        DbServerImpl.log.debug("Creating table PROJECTS if not exist");

        final String createTableProject =
                "CREATE TABLE IF NOT EXISTS PROJECTS " +
                        "(ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "name TEXT NOT NULL," +
                        "createDate REAL NOT NULL)";

        this.jdbcTemplate.execute(createTableProject);

        DbServerImpl.log.debug("Table PROJECTS successfully created");
    }
}
