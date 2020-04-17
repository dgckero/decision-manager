/**
 * @author david
 */

package com.dgc.dm.core.db.dao;

import com.dgc.dm.core.db.model.Project;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class DataDaoImpl extends CommonDao implements DataDao {

    @Override
    public void createDataTable(Map<String, Class<?>> columns, Project project) {
        log.info(String.format("****** Creating table: %s ******", project.getCommonDataTableName()));

        String commonDataTableStatements = "CREATE TABLE IF NOT EXISTS " + project.getCommonDataTableName() + " (rowId INTEGER, ";
        for (final Map.Entry<String, Class<?>> column : columns.entrySet()) {
            commonDataTableStatements += column.getKey() + " " + DatabaseColumnType.getDBClassByColumnType(column.getValue().getSimpleName()) + ",";
        }
        final String foreignKey = ", project INTEGER NOT NULL,FOREIGN KEY(project) REFERENCES PROJECTS(id), " +
                "PRIMARY KEY (rowId, project) )";
        commonDataTableStatements = commonDataTableStatements.replaceAll("[,]$", foreignKey);
        getJdbcTemplate().execute(commonDataTableStatements);

        log.info(project.getCommonDataTableName() + " table successfully created");
    }

    @Override
    public void persistData(String insertSentence, List<Object[]> infoToBePersisted) {
        log.info("****** Persisting Excel rows into commonDatas table: %s ******");
        getJdbcTemplate().batchUpdate(insertSentence, infoToBePersisted);
        log.info("****** Persisted Excel rows into commonDatas table: %s ******");
    }

    @Override
    public List<Map<String, Object>> getCommonData(Project project) {
        log.info("Getting all info from table: " + project.getCommonDataTableName());
        final List<Map<String, Object>> entities = getJdbcTemplate().queryForList("Select * from " + project.getCommonDataTableName() + " where project=" + project.getId());
        log.info("Got all info from table: " + project.getCommonDataTableName());
        return entities;
    }

    @Override
    public void deleteCommonData(Project project) {
        log.debug("Deleting all registers for project " + project);
        getJdbcTemplate().execute("DELETE FROM " + project.getCommonDataTableName());
        log.debug("Registers successfully deleted for project " + project);
    }
}
