/**
 * @author david
 */

package com.dgc.dm.core.db.service;

import lombok.extern.log4j.Log4j2;
import org.h2.tools.Server;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Log4j2
@Service
public class DbServerImpl implements DbServer {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Bean(initMethod = "start", destroyMethod = "stop")
    public Server inMemoryH2DatabaseServer() throws SQLException {
        return Server.createTcpServer("-tcp", "-tcpAllowOthers", "-tcpPort", "9091");
    }

    @Override
    public void createFilterTable(Map<String, Class<?>> props) {
        log.info(String.format("****** Creating table: %s ******", "Filters"));


        String sqlStatements[] = {
                "drop table `filters` if exists",
                "create table `filters` (`id` serial,`name` varchar(100),`class` varchar(50))",
                "insert into `filters` (`name`, `class`) values ('rowId','int')"
        };

        List<String> statements = new ArrayList<>();
        statements.addAll(Arrays.asList(sqlStatements));

        for (Map.Entry<String, Class<?>> prop : props.entrySet()) {
            statements.add("insert into `filters` (`name`, `class`) values('" + prop.getKey() + "','" + prop.getValue().getName() + "') ");
        }

        statements.stream().forEach(sql -> {
            log.debug(sql);
            jdbcTemplate.execute(sql);
        });

        log.info(String.format("****** table: %s  successfully created ******", "Filters"));
    }

}
