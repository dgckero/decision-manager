/*
  @author david
 */

package com.dgc.dm.core.db.dao;

import lombok.AccessLevel;
import lombok.Getter;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;

@Getter(AccessLevel.PROTECTED)
class CommonDao {
    @Autowired
    protected SessionFactory sessionFactory;
}
