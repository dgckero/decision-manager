/*
  @author david
 */

package com.dgc.dm.core.db.repository;

import com.dgc.dm.core.db.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Integer> {
}
