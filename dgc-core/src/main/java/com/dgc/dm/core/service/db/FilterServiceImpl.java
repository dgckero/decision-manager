/*
  @author david
 */

package com.dgc.dm.core.service.db;

import com.dgc.dm.core.db.dao.FilterDao;
import com.dgc.dm.core.db.model.Filter;
import com.dgc.dm.core.db.model.Project;
import com.dgc.dm.core.dto.FilterDto;
import com.dgc.dm.core.dto.ProjectDto;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.validation.constraints.Email;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
public class FilterServiceImpl extends CommonServer implements FilterService {

    @Autowired
    FilterDao filterDao;

    /**
     * Create FILTERS table, set contactFilter to true and persist filterList into FILTERS table
     *
     * @param filterList
     * @param project
     */
    @Override
    public void persistFilterList(List<FilterDto> filterList, ProjectDto project) {
        log.info("Creating filters");

        filterDao.createFilterTable(getModelMapper().map(project, Project.class));

        log.debug("Mapping filterDto list to FilterEntity list");
        List<Filter> filterEntityList = getModelMapper().map(
                filterList,
                (new TypeToken<List<Filter>>() {
                }.getType())
        );
        log.debug("Setting true on contact filter that contains Email information");
        filterEntityList = markContactFilter(filterEntityList);

        filterDao.persistFilterList(filterEntityList);
    }

    /**
     * Set true on contact filter that contains Email information
     *
     * @param filterList
     * @return filterList having contact filter updated
     */
    private List<Filter> markContactFilter(List<Filter> filterList) {
        AtomicInteger itemNumber = new AtomicInteger();
        filterList.stream()
                .filter(flt -> {
                    String filterClass = flt.getFilterClass();
                    itemNumber.getAndIncrement();
                    return filterClass.equals(Email.class.getSimpleName());
                })
                .forEach(
                        s ->
                                filterList.set(
                                        (itemNumber.get() - 1),
                                        s.toBuilder().
                                                filterClass(String.class.getSimpleName()).
                                                contactFilter(Boolean.TRUE).
                                                build()
                                )
                );
        return filterList;
    }

    /**
     * Get all filters stored on FILTERS table
     *
     * @return all filters
     */
    @Override
    public List<Map<String, Object>> getFilters() {
        log.info("Getting All filters");
        List<Map<String, Object>> filters = filterDao.getFilters();
        log.info("Found " + filters.size() + " filters");
        return filters;
    }

    /**
     * Get all project's filters
     *
     * @param project
     * @return project's filters
     */
    @Override
    public List<Map<String, Object>> getFilters(ProjectDto project) {
        log.info("Getting filters for project " + project);
        List<Map<String, Object>> filters = filterDao.getFilters(getModelMapper().map(project, Project.class));
        log.info("Found " + filters.size() + " filters for project " + project);
        return filters;
    }

    /**
     * Update filters on database
     *
     * @param filters
     */
    @Override
    public void updateFilters(List<FilterDto> filters) {
        log.info("Updating " + filters.size() + " filters ");
        filterDao.updateFilters(getModelMapper().map(filters, (new TypeToken<List<Filter>>() {
        }.getType())));
        log.info("Filters successfully updated ");
    }

    /**
     * Getting contact filter (contactFilter = true) by project
     *
     * @param project
     * @return
     */
    @Override
    public FilterDto getContactFilter(ProjectDto project) {
        FilterDto result;
        log.info("Getting contact filter for project " + project);

        Filter filterEntity = filterDao.getContactFilter(getModelMapper().map(project, Project.class));
        if (filterEntity == null) {
            log.info("No contact filter found for project " + project);
            result = null;
        } else {
            FilterDto filterDto = getModelMapper().map(filterEntity, FilterDto.class);
            log.info("Found contact filter " + filterDto + " for project " + project);
            result = filterDto;
        }
        return result;
    }
}
