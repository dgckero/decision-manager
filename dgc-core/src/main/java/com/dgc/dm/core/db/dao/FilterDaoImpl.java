/*
  @author david
 */

package com.dgc.dm.core.db.dao;

import com.dgc.dm.core.db.model.Filter;
import com.dgc.dm.core.db.model.Project;
import com.dgc.dm.core.db.repository.FilterRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class FilterDaoImpl extends CommonDao implements FilterDao {

    @Autowired
    private FilterRepository filterRepository;

    /**
     * Create FILTERS table and insert rowId Filter
     *
     * @param project
     */
    @Override
    public final void createFilterTable(final Project project) {
        log.info(String.format("****** Creating table: %s ******", "Filters"));

        final Iterable<String> filterTableStatements = new StringArrayList(project);

        filterTableStatements.forEach(sql -> {
            log.debug(sql);
            this.getJdbcTemplate().execute(sql);
        });
        log.info("FILTERS table successfully created");
    }

    /**
     * Save filterList on FILTERS table
     *
     * @param filterList
     */
    @Override
    public final void persistFilterList(final List<Filter> filterList) {
        log.debug("Persisting filters got from Excel");
        this.filterRepository.saveAll(filterList);
        log.debug("Persisted filters got from Excel");
    }

    /**
     * Get all filters from FILTERS table
     *
     * @return filters from FILTERS table
     */
    @Override
    public final List<Map<String, Object>> getFilters() {
        log.info("Getting Filters");
        final List<Map<String, Object>> filters = this.getJdbcTemplate().queryForList("Select * from FILTERS");
        log.info("Got filters");
        return filters;
    }

    /**
     * Get all project's filters from FILTERS table
     *
     * @param project
     * @return all project's filters
     */
    @Override
    public final List<Map<String, Object>> getFilters(final Project project) {
        log.info("Getting Filters by project {}", project);
        final List<Map<String, Object>> filters = this.getJdbcTemplate().queryForList("Select * from FILTERS where project=" + project.getId());
        log.info("Got filters");
        return filters;
    }

    /**
     * Update filterList on FILTERS table
     *
     * @param filters
     */
    @Override
    public final void updateFilters(final List<Filter> filters) {
        log.info("Updating filters ");
        this.filterRepository.saveAll(filters);
        log.info("Filters updated");
    }

    /**
     * Getting contact filter (contactFilter = true) from FILTERS by project
     *
     * @param project
     * @return project's filter having contactFilter=true
     */
    @Override
    public final Filter getContactFilter(final Project project) {
        log.info("Getting Filters having contactFilter active for project {}", project);
        Filter filter = null;
        try {
            final String sql = "Select * from FILTERS where contactFilter=? and project=?";
            filter = this.getJdbcTemplate().queryForObject(sql, new Object[]{Integer.valueOf(1), project.getId()}, (rs, rowNum) ->
                    new Filter(
                            Integer.valueOf(rs.getInt("id")),
                            rs.getString("name"),
                            rs.getString("class"),
                            rs.getString("value"),
                            Boolean.valueOf(rs.getBoolean("active")),
                            Boolean.valueOf(rs.getBoolean("contactFilter")),
                            Project.builder().id(Integer.valueOf(rs.getInt("project"))).build()
                    ));

            log.info("Got filter {}", filter);

        } catch (final EmptyResultDataAccessException e) {
            log.info("No filter found having contactFilter active for project {}", project);
        }
        return filter;
    }

    private static class StringArrayList extends ArrayList<String> {
        private static final long serialVersionUID = 5389826304558701396L;

        public StringArrayList(Project project) {
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
