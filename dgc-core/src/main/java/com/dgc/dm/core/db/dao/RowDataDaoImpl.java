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

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public final void createRowDataTable(Map<String, Class<?>> columns, Project project) {
        log.info(String.format("****** Creating table: %s ******", project.getRowDataTableName()));

        StringBuilder commonDataTableStatements = new StringBuilder("CREATE TABLE IF NOT EXISTS " + project.getRowDataTableName() + " (rowId INTEGER, ");
        for (Map.Entry<String, Class<?>> column : columns.entrySet()) {
            String columnName = column.getKey();
            Class<?> columnClass = column.getValue();
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
        getJdbcTemplate().execute(commonDataTableStatements.toString());

        log.info("{} table successfully created", project.getRowDataTableName());
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public final void persistRowData(String insertSentence, List<Object[]> infoToBePersisted) {
        log.info("****** Persisting Excel rows into commonDatas table: %s ******");
        getJdbcTemplate().batchUpdate(insertSentence, infoToBePersisted);
        log.info("****** Persisted Excel rows into commonDatas table: %s ******");
    }

    @Override
    public final List<Map<String, Object>> getRowData(Project project) {
        log.info("Getting all info from table: {}", project.getRowDataTableName());
        List<Map<String, Object>> entities = getJdbcTemplate().queryForList("Select * from " + project.getRowDataTableName() + " where project=" + project.getId());
        log.info("Got all info from table: {}", project.getRowDataTableName());
        return entities;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public final void deleteRowData(Project project) {
        log.debug("Deleting all registers for project {}", project);
        getJdbcTemplate().execute("DELETE FROM " + project.getRowDataTableName());
        log.debug("Registers successfully deleted for project {}", project);
    }

    @Override
    public final Integer getRowDataSize(Project project) {
        log.debug("Getting common data size for project {}", project);
        Integer count = getJdbcTemplate().queryForObject("SELECT count(*) FROM " + project.getRowDataTableName(), Integer.class);
        log.debug("common data size {} for project {}", count, project);

        return count;
    }
}
