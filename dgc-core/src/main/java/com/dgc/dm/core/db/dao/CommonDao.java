/*
  @author david
 */

package com.dgc.dm.core.db.dao;

import lombok.AccessLevel;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

@Getter(AccessLevel.PROTECTED)
class CommonDao {
    @Autowired
    private JdbcTemplate jdbcTemplate;
}
