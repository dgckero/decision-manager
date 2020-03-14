/*
  @author david
 */

package com.dgc.dm.core.db.dao;

import com.dgc.dm.core.db.model.CommonEntity;
import org.springframework.data.repository.CrudRepository;


public interface CommonRepository extends CrudRepository<CommonEntity, Integer> {
}
