/*
  @author david
 */

package com.dgc.dm.core.db.repository;

import com.dgc.dm.core.db.model.RowData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RowDataRepository extends JpaRepository<RowData, Integer> {
}
