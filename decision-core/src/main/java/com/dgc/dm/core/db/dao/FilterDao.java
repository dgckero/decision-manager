/*
  @author david
 */

package com.dgc.dm.core.db.dao;

import com.dgc.dm.core.db.model.Filter;
import com.dgc.dm.core.db.model.Project;

import java.util.List;

public interface FilterDao {

    void createFilterTable(Project project);

    void persistFilterList(List<Filter> filterList);

    List<Filter> getFilters(Project project);

    void updateFilters(List<Filter> activeFilters);

    Filter getContactFilter(Project project);

}
