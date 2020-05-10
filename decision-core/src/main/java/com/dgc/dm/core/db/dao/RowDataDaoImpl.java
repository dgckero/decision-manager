/*
  @author david
 */

package com.dgc.dm.core.db.dao;

import com.dgc.dm.core.db.model.Project;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class RowDataDaoImpl extends CommonDao implements RowDataDao {

    /**
     * Create table with name project.RowDataTableName
     * based on columns
     *
     * @param columns columns of new table
     * @param project
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public final void createRowDataTable (final Map<String, Class<?>> columns, final Project project) {
        log.debug("[INIT] Creating table {}", project.getRowDataTableName());

        StringBuilder commonDataTableStatements = new StringBuilder("CREATE TABLE IF NOT EXISTS " + project.getRowDataTableName() +
                " (rowId INTEGER, dataCreationDate TEXT NOT NULL, lastUpdatedDate TEXT,");
        for (final Map.Entry<String, Class<?>> column : columns.entrySet()) {
            final String columnName = column.getKey();
            final Class<?> columnClass = column.getValue();
            if (!StringUtils.isEmpty(columnName) && (null != columnClass)) {
                commonDataTableStatements.append("'" + columnName + "'");
                commonDataTableStatements.append(" ");
                commonDataTableStatements.append(DatabaseColumnType.getDBClassByColumnType(column.getValue().getSimpleName()));
                commonDataTableStatements.append(",");
            }
        }
        final String foreignKey = ", project INTEGER NOT NULL,FOREIGN KEY(project) REFERENCES PROJECTS(id), " +
                "PRIMARY KEY (rowId, project) )";
        commonDataTableStatements = new StringBuilder(commonDataTableStatements.toString().replaceAll("[,]$", foreignKey));
        log.debug("Executing script {}", commonDataTableStatements);
        this.sessionFactory.getCurrentSession().createSQLQuery(commonDataTableStatements.toString()).executeUpdate();

        log.debug("[END] {} table successfully created", project.getRowDataTableName());
    }

    /**
     * Persist infoToBePersisted into rowData table
     *
     * @param insertSentence
     * @param infoToBePersisted
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public final void persistRowData (final String insertSentence, final List<Object[]> infoToBePersisted) {
        log.debug("[INIT] Persisting Excel rows into commonDatas table");
        for (int i = 0; i < infoToBePersisted.size(); i++) {
            final Object[] info = infoToBePersisted.get(i);
            this.sessionFactory.getCurrentSession().createSQLQuery(this.generateSqlSentence(insertSentence, info)).executeUpdate();
            if (i % 20 == 0) {
                //flush a batch of inserts and release memory:
                this.sessionFactory.getCurrentSession().flush();
                this.sessionFactory.getCurrentSession().clear();
            }
        }
        log.debug("[END] Persisted Excel rows into commonDatas table");
    }

    /**
     * Concatenate insert Sql sentence with rowData values
     *
     * @param insertSentence
     * @param rowData
     * @return SQL Insert Query
     */
    private String generateSqlSentence (final String insertSentence, final Object[] rowData) {
        final String[] tokens = insertSentence.split("\\?", rowData.length + 1);
        final StringBuilder sBuilder = new StringBuilder();

        for (int i = 0; i < rowData.length; i++) {
            final Object data = rowData[i];
            if (null == data) {
                sBuilder.append(tokens[i]).append("null");
            } else if (data instanceof Integer) {
                sBuilder.append(tokens[i]).append(data);
            } else {
                sBuilder.append(tokens[i]).append("'" + data + "'");
            }
        }
        final String result = sBuilder.append(tokens[rowData.length]).toString();

        return result;
    }

    /**
     * Get all information from project.RowDataTableName
     *
     * @param project
     * @return all information from project.RowDataTableName
     */
    @Override
    public final List<Map<String, Object>> getRowData (final Project project) {
        log.debug("[INIT] Getting all info from table: {}", project.getRowDataTableName());
        final List<Map<String, Object>> entities = this.getJdbcTemplate().queryForList("Select * from " + project.getRowDataTableName() + " where project=" + project.getId());
        log.debug("[END] Got all info from table: {}", project.getRowDataTableName());
        return entities;
    }

    /**
     * Delete all information from project.RowDataTableName
     *
     * @param project
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public final void deleteRowData (final Project project) {
        log.debug("[INIT] Deleting all registers for project {}", project);
        this.sessionFactory.getCurrentSession().createSQLQuery("DELETE FROM " + project.getRowDataTableName());
        log.debug("[END] Registers successfully deleted for project {}", project);
    }

    /**
     * Get number of rows on table project.RowDataTableName
     *
     * @param project
     * @return number of rows on table project.RowDataTableName
     */
    @Override
    public final Integer getRowDataSize (final Project project) {
        log.debug("[INIT] Getting common data size for project {}", project);
        final Integer count = this.getJdbcTemplate().queryForObject("SELECT count(*) FROM " + project.getRowDataTableName(), Integer.class);
        log.debug("[END] common data size {} for project {}", count, project);

        return count;
    }
}
