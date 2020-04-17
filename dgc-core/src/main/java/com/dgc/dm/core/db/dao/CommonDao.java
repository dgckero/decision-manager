/*
  @author david
 */

package com.dgc.dm.core.db.dao;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

@Getter
public class CommonDao {

    @Autowired
    private JdbcTemplate jdbcTemplate;

}
