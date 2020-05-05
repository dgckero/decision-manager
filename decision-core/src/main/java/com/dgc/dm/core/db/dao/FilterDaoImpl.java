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
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class FilterDaoImpl extends CommonDao implements FilterDao {

    private static final String SELECT_CONTACT_FILTER = "Select * from FILTERS where contactFilter=? and project=?";
    @Autowired
    private FilterRepository filterRepository;

    /**
     * Map ResultSet to Filter object
     *
     * @param rs
     * @param rowNum
     * @return
     * @throws SQLException
     */
    private static Filter mapResultSet (ResultSet rs, int rowNum) throws SQLException {
        return new Filter(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("class"),
                rs.getString("value"),
                rs.getBoolean("active"),
                rs.getBoolean("contactFilter"),
                Project.builder().id(rs.getInt("project")).
                        build()
        );
    }

    /**
     * Create FILTERS table and insert rowId Filter
     *
     * @param project
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public final void createFilterTable (final Project project) {
        log.debug("[INIT] Creating table: Filters");

        final Iterable<String> filterTableStatements = new StringArrayList(project);
        filterTableStatements.forEach(sql -> {
            log.debug(sql);
            this.sessionFactory.getCurrentSession().createSQLQuery(sql).executeUpdate();
        });
        log.debug("[END] FILTERS table successfully created");
    }

    /**
     * Save filterList on FILTERS table
     *
     * @param filterList
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public final void persistFilterList (final List<Filter> filterList) {
        log.debug("[INIT] Persisting filters got from Excel");

        for (int i = 0; i < filterList.size(); i++) {
            this.sessionFactory.getCurrentSession().save(filterList.get(i));
            if (i % 20 == 0) {
                //flush a batch of inserts and release memory:
                this.sessionFactory.getCurrentSession().flush();
                this.sessionFactory.getCurrentSession().clear();
            }
        }

        log.debug("[END] Persisted filters got from Excel");
    }

    /**
     * Get all filters from FILTERS table
     *
     * @return filters from FILTERS table
     */
    @Override
    public final List<Map<String, Object>> getFilters ( ) {
        log.debug("[INIT] Getting Filters");
        final List<Map<String, Object>> filters = this.getJdbcTemplate().queryForList("Select * from FILTERS");
        log.debug("[END] Got filters");
        return filters;
    }

    /**
     * Get all project's filters from FILTERS table
     *
     * @param project
     * @return all project's filters
     */
    @Override
    public final List<Map<String, Object>> getFilters (final Project project) {
        log.debug("[INIT] Getting Filters by project {}", project);
        final List<Map<String, Object>> filters = this.getJdbcTemplate().queryForList("Select * from FILTERS where project=" + project.getId());
        log.debug("[END] Got filters");
        return filters;
    }

    /**
     * Update filterList on FILTERS table
     *
     * @param filterList
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public final void updateFilters (final List<Filter> filterList) {
        log.debug("[INIT] Updating filters ");
        for (int i = 0; i < filterList.size(); i++) {
            this.sessionFactory.getCurrentSession().merge(filterList.get(i));
            if (i % 5 == 0) {
                //flush a batch of inserts and release memory:
                this.sessionFactory.getCurrentSession().flush();
                this.sessionFactory.getCurrentSession().clear();
            }
        }
        log.debug("[END] Filters updated");
    }

    /**
     * Getting contact filter (contactFilter = true) from FILTERS by project
     *
     * @param project
     * @return project's filter having contactFilter=true
     */
    @Override
    public final Filter getContactFilter (final Project project) {
        log.debug("[INIT] Getting Filters having contactFilter active for project {}", project);
        Filter filter = null;
        try {
            final String sql = SELECT_CONTACT_FILTER;
            filter = this.getJdbcTemplate().queryForObject(sql, new Object[]{Integer.valueOf(1), project.getId()}, FilterDaoImpl::mapResultSet);

            log.debug("[END] Got filter {}", filter);

        } catch (final EmptyResultDataAccessException e) {
            log.warn("No filter found having contactFilter active for project {}", project);
        }
        return filter;
    }

    private static class StringArrayList extends ArrayList<String> {
        private static final long serialVersionUID = 5389826304558701396L;

        /**
         * Generate create table script by project
         *
         * @param project
         */
        StringArrayList (Project project) {
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