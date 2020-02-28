/**
 * @author david
 */

package com.dgc.dm.core.db.service;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Log4j2
@Service
public class DbServerImpl implements DbServer {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void createAndPopulateFilterTable(final Map<String, Class<?>> columns) {
        log.info(String.format("****** Creating table: %s ******", "Filters"));

        List<String> statements = new ArrayList<String>() {
            {
                add("SET SCHEMA DECISION_DB");
                add("drop table `filters` if exists");
                add("create table `filters` (`id` serial,`name` varchar(100),`class` varchar(50))");
                add("insert into `filters` (`name`, `class`) values ('rowId','int')");
            }
        };

        String recordsTableCreate = "create table `commonDatas` (`id` INT, ";

        for (Map.Entry<String, Class<?>> column : columns.entrySet()) {
            statements.add("insert into `filters` (`name`, `class`) values('" + column.getKey() + "','" + column.getValue().getName() + "') ");
            recordsTableCreate += column.getKey() + " varchar(100),";
            //TODO add row class based on column class value
        }

        statements.forEach(sql -> {
            log.debug(sql);
            jdbcTemplate.execute(sql);
        });

        recordsTableCreate = recordsTableCreate.replaceAll("[,]$", ") ");
        jdbcTemplate.execute(recordsTableCreate);

        log.info(String.format("****** table: %s  successfully created ******", "Filters"));
    }

    public void persistExcelRows(final List<String> infoToBePersisted) {
        log.info("****** Persisting Excel rows into commonDatas table: %s ******");

        infoToBePersisted.forEach(sql -> {
            log.debug(sql);
            jdbcTemplate.execute(sql);
        });

        log.info("****** Persisted Excel rows into commonDatas table: %s ******");
    }

}
