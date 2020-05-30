/*
  @author david
 */

package com.dgc.dm.core.db.dao;

import com.dgc.dm.core.db.model.Filter;
import com.dgc.dm.core.db.model.Project;
import lombok.extern.log4j.Log4j2;
import org.hibernate.query.Query;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Log4j2
@Service
public class FilterDaoImpl extends CommonDao implements FilterDao {
    /**
     * Create FILTERS table and insert rowId Filter
     *
     * @param project
     */
    @Override
    public void createFilterTable(final Project project) {
        log.debug("[INIT] Creating table: Filters");

        final Iterable<String> filterTableStatements = new StringArrayList(project);
        filterTableStatements.forEach(sql -> {
            log.debug(sql);
            this.sessionFactory.getCurrentSession().createNativeQuery(sql).executeUpdate();
        });
        log.debug("[END] FILTERS table successfully created");
    }

    /**
     * Save filterList on FILTERS table
     *
     * @param filterList
     */
    @Override
    public void persistFilterList(final List<Filter> filterList) {
        log.debug("[INIT] Persisting filters got from Excel");

        for (int i = 0; i < filterList.size(); i++) {
            Filter filter = filterList.get(i);
            this.sessionFactory.getCurrentSession().save(filter);
        }

        log.debug("[END] Persisted filters got from Excel");
    }

    /**
     * Get all project's filters from FILTERS table
     *
     * @param project
     * @return all project's filters
     */
    @Override
    public final List<Filter> getFilters(final Project project) {
        log.debug("[INIT] Getting Filters by project {}", project);
        Query query = sessionFactory.getCurrentSession().createQuery("from Filter f where f.project =:project");
        query.setParameter("project", project);

        List<Filter> filters = query.list();
        log.debug("[END] Got filters");
        return filters;
    }

    /**
     * Update filterList on FILTERS table
     *
     * @param filterList
     */
    @Override
    public void updateFilters(final List<Filter> filterList) {
        log.debug("[INIT] Updating filters ");
        for (int i = 0; i < filterList.size(); i++) {
            Filter updatedFilter = filterList.get(i);
            this.sessionFactory.getCurrentSession().merge(updatedFilter);
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
    public final Filter getContactFilter(final Project project) {
        log.debug("[INIT] Getting Filters having contactFilter active for project {}", project);
        Filter filter = null;
        try {
            Query query = this.sessionFactory.getCurrentSession().createQuery("from Filter f where contactFilter= :contactFilter and project= :project");
            query.setParameter("contactFilter", true);
            query.setParameter("project", project);

            filter = (Filter) query.uniqueResult();

        } catch (final EmptyResultDataAccessException e) {
            log.warn("No filter found having contactFilter active for project {}", project);
        }
        log.debug("[END] Got filter {}", filter);
        return filter;
    }

    private static class StringArrayList extends ArrayList<String> {
        private static final long serialVersionUID = 5389826304558701396L;

        /**
         * Generate create table script by project
         *
         * @param project
         */
        StringArrayList(Project project) {
            this.add("CREATE TABLE IF NOT EXISTS FILTERS " +
                    "( ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "name TEXT," +
                    "class TEXT, " +
                    "active INTEGER default 0," +
                    "contactFilter INTEGER default 0," +
                    "value TEXT default NULL," +
                    "project INTEGER NOT NULL," +
                    "dataCreationDate TEXT NOT NULL," +
                    "lastUpdatedDate TEXT," +
                    "FOREIGN KEY(project) REFERENCES PROJECTS(id)," +
                    "CONSTRAINT UQ_NAME_PROJ UNIQUE (name, project) )");
            this.add("INSERT INTO FILTERS (name, class, project, dataCreationDate) values ('rowId','java.lang.Integer','" + project.getId() + "','" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")) + "')");
        }
    }
}
