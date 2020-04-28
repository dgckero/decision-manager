/*
  @author david
 */

package com.dgc.dm.core.db.repository;

import com.dgc.dm.core.db.model.Filter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public interface FilterRepository extends JpaRepository<Filter, Integer> {
}
