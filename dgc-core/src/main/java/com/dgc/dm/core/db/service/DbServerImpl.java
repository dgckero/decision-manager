/**
 * @author david
 */

package com.dgc.dm.core.db.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class DbServerImpl implements DbServer {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void createAndPopulateFilterTable(final Map<String, Class<?>> columns) {
        log.info(String.format("****** Creating table: %s ******", "Filters"));

        List<String> filterTableStatements = new ArrayList<String>() {
            {
                add("CREATE TABLE IF NOT EXISTS FILTERS (ID INTEGER PRIMARY KEY AUTOINCREMENT,name TEXT,class TEXT)");
                add("INSERT INTO FILTERS (name, class) values ('rowId','int')");
            }
        };

        String commonDataTableStatements = "CREATE TABLE IF NOT EXISTS COMMONDATAS (rowId INTEGER PRIMARY KEY, ";

        for (Map.Entry<String, Class<?>> column : columns.entrySet()) {
            filterTableStatements.add("INSERT INTO FILTERS (name, class) values('" + column.getKey() + "','" + column.getValue().getName() + "') ");
            commonDataTableStatements += column.getKey() + " TEXT,";
            //TODO add row class based on column class value
        }

        filterTableStatements.forEach(sql -> {
            log.debug(sql);
            jdbcTemplate.execute(sql);
        });

        commonDataTableStatements = commonDataTableStatements.replaceAll("[,]$", ") ");
        jdbcTemplate.execute(commonDataTableStatements);

        log.info(String.format("****** table: %s  successfully created ******", "Filters"));
    }

    public void persistExcelRows(final String insertSentence, final List<Object[]> infoToBePersisted) {
        log.info("****** Persisting Excel rows into commonDatas table: %s ******");
        jdbcTemplate.batchUpdate(insertSentence, infoToBePersisted);
        log.info("****** Persisted Excel rows into commonDatas table: %s ******");
    }

    public void getFilters() {
        log.info("Getting Filters");

        List<Map<String, Object>> filters = jdbcTemplate.queryForList("Select * from FILTERS");


        log.info("Got filters");
    }

}
