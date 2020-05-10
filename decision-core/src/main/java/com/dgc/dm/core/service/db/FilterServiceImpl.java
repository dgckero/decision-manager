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
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.Email;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
public class FilterServiceImpl extends CommonServer implements FilterService {

    @Autowired
    private
    FilterDao filterDao;

    /**
     * Create FILTERS table, set contactFilter to true and persist filterList into FILTERS table
     *
     * @param filterList
     * @param project
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void persistFilterList (final List<FilterDto> filterList, final ProjectDto project) {
        log.info("[INIT] persistFilterList filterList: {}, project: {}", filterList, project);

        this.filterDao.createFilterTable(this.getModelMapper().map(project, Project.class));
        log.debug("Mapping filterDto list to FilterEntity list");
        List<Filter> filterEntityList = this.getModelMapper().map(
                filterList,
                (new TypeToken<List<Filter>>() {
                }.getType())
        );
        log.debug("Setting true on contact filter that contains Email information");
        filterEntityList = this.markContactFilter(filterEntityList);
        this.filterDao.persistFilterList(filterEntityList);

        log.info("[END] persistFilterList");
    }

    /**
     * Set true on contact filter that contains Email information
     *
     * @param filterList
     * @return filterList having contact filter updated
     */
    private List<Filter> markContactFilter (final List<Filter> filterList) {
        log.debug("[INIT] markContactFilter filterList: {}", filterList);
        final AtomicInteger itemNumber = new AtomicInteger();
        filterList.stream()
                .filter(flt -> {
                    final String filterClass = flt.getFilterClass();
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
        log.debug("[END] markContactFilter filterList: {}", filterList);
        return filterList;
    }

    /**
     * Get all filters stored on FILTERS table
     *
     * @return all filters
     */
    @Override
    public List<Map<String, Object>> getFilters ( ) {
        log.debug("[INIT] Getting All filters");
        final List<Map<String, Object>> filters = this.filterDao.getFilters();
        log.debug("[END] Found " + filters.size() + " filters");
        return filters;
    }

    /**
     * Get all project's filters
     *
     * @param project
     * @return project's filters
     */
    @Override
    public List<Map<String, Object>> getFilters (final ProjectDto project) {
        log.debug("[INIT] Getting filters for project " + project);
        final List<Map<String, Object>> filters = this.filterDao.getFilters(this.getModelMapper().map(project, Project.class));
        log.debug("[END] Found " + filters.size() + " filters for project " + project);
        return filters;
    }

    /**
     * Update filters on database
     *
     * @param filters
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void updateFilters (final List<FilterDto> filters) {
        log.debug("[INIT] Updating " + filters.size() + " filters ");
        this.filterDao.updateFilters(this.getModelMapper().map(filters, (new TypeToken<List<Filter>>() {
        }.getType())));
        log.debug("[END] Filters successfully updated ");
    }

    /**
     * Getting contact filter (contactFilter = true) by project
     *
     * @param project
     * @return
     */
    @Override
    public FilterDto getContactFilter (final ProjectDto project) {
        log.debug("[INIT] getContactFilter for project " + project);
        final FilterDto result;

        final Filter filterEntity = this.filterDao.getContactFilter(this.getModelMapper().map(project, Project.class));
        if (filterEntity == null) {
            log.debug("No contact filter found for project " + project);
            result = null;
        } else {
            final FilterDto filterDto = this.getModelMapper().map(filterEntity, FilterDto.class);
            log.debug("Found contact filter " + filterDto + " for project " + project);
            result = filterDto;
        }
        log.debug("[END] getContactFilter for project " + project);
        return result;
    }
}
