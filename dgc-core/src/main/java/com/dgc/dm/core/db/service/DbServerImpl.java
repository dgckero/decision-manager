/*
  @author david
 */
package com.dgc.dm.core.db.service;

import com.dgc.dm.core.db.model.Filter;
import com.dgc.dm.core.db.repository.FilterRepository;
import com.dgc.dm.core.dto.FilterDto;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

enum CLAZZ {
    DATE(java.util.Date.class.getSimpleName()),
    STRING(java.lang.String.class.getSimpleName()),
    DOUBLE(java.lang.Double.class.getSimpleName()),
    INTEGER(java.lang.Integer.class.getSimpleName());

    private String simpleNameClass;

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
    FilterRepository filterRepository;

    @Autowired
    private ModelMapper modelMapper;

    private String getDBClassByColumnType(String columnClassName) {

        CLAZZ cl = CLAZZ.valueOf(columnClassName.toUpperCase());

        switch (cl) {
            case DATE:
            case STRING:
                return "TEXT";
            case INTEGER:
                return "INTEGER";
            case DOUBLE:
                return "REAL";
            default:
                return "TEXT";
        }
    }

    @Override
    public void createAndPopulateFilterTable(final Map<String, Class<?>> columns) {
        log.info(String.format("****** Creating table: %s ******", "Filters"));

        List<String> filterTableStatements = new ArrayList<String>() {
            {
                add("CREATE TABLE IF NOT EXISTS FILTERS " +
                        "(ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "name TEXT," +
                        "class TEXT, " +
                        "active INTEGER default 0," +
                        "value TEXT default NULL)");
                add("INSERT INTO FILTERS (name, class) values ('rowId','java.lang.Integer')");
            }
        };

        filterTableStatements.forEach(sql -> {
            log.debug(sql);
            jdbcTemplate.execute(sql);
        });
        log.info("FILTERS table successfully created");

        String commonDataTableStatements = "CREATE TABLE IF NOT EXISTS COMMONDATAS (rowId INTEGER PRIMARY KEY, ";
        List<Filter> filterList = new ArrayList<>();
        for (Map.Entry<String, Class<?>> column : columns.entrySet()) {

            filterList.add(Filter.builder().
                    name(column.getKey()).
                    filterClass(column.getValue().getSimpleName()).
                    active(Boolean.FALSE).
                    build());

            commonDataTableStatements += column.getKey() + " " + getDBClassByColumnType(column.getValue().getSimpleName()) + ",";
        }

        log.debug("Persisting filters got from Excel");
        filterRepository.saveAll(filterList);
        log.debug("Persisted filters got from Excel");

        commonDataTableStatements = commonDataTableStatements.replaceAll("[,]$", ") ");
        jdbcTemplate.execute(commonDataTableStatements);

        log.info(String.format("****** table: %s  successfully created ******", "Filters"));
    }

    public void persistExcelRows(final String insertSentence, final List<Object[]> infoToBePersisted) {
        log.info("****** Persisting Excel rows into commonDatas table: %s ******");
        jdbcTemplate.batchUpdate(insertSentence, infoToBePersisted);
        log.info("****** Persisted Excel rows into commonDatas table: %s ******");
    }

    public List<Map<String, Object>> getFilters() {
        log.info("Getting Filters");
        List<Map<String, Object>> filters = jdbcTemplate.queryForList("Select * from FILTERS");
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

}
