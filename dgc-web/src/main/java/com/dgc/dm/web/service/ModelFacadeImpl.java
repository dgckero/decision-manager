/*
  @author david
 */

package com.dgc.dm.web.service;

import com.dgc.dm.core.dto.FilterCreationDto;
import com.dgc.dm.core.dto.FilterDto;
import com.dgc.dm.core.dto.ProjectDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;


@Slf4j
@Service
class ModelFacadeImpl extends CommonFacade implements ModelFacade {

    private static final Integer CONTACT_FILTER = 1;

    private static List<FilterDto> getFilterListByModelMap(Collection<Map<String, Object>> filterList, ProjectDto project) {
        log.debug("Parsing List<Map<String,Object>> to List<FilterDto>");

        List<FilterDto> filterDtoList = new ArrayList<>(filterList.size());

        Iterator<Map<String, Object>> entryIterator = filterList.iterator();
        while (entryIterator.hasNext()) {
            Map<String, Object> filterIterator = entryIterator.next();

            String filterName = (String) filterIterator.get("name");
            if ("rowId".equals(filterName)) {
                // Don't send rowId to decision view
                entryIterator.remove();
                log.debug("Removed filter rowId from filterList");
            } else {
                FilterDto filter = FilterDto.builder().
                        id((Integer) filterIterator.get("ID")).
                        name(filterName).
                        filterClass((String) filterIterator.get("class")).
                        contactFilter(filterIterator.get("contactFilter").equals(CONTACT_FILTER)).
                        project(project).
                        build();
                filterDtoList.add(filter);
                log.debug("Added filter {}", filter);
            }
        }
        log.debug("Generated List<filterDto>");
        return filterDtoList;
    }

    @Override
    public final FilterCreationDto getFilterCreationDto(ProjectDto project, Collection<Map<String, Object>> filterList) {
        log.info("Generating FilterCreationDto");
        final List<FilterDto> filterDtoList = getFilterListByModelMap(filterList, project);
        log.info("Adding {} filters to FilterCreationDto", filterDtoList.size());
        FilterCreationDto filterCreationDto = new FilterCreationDto(filterDtoList);
        log.info("FilterCreationDto successfully created");
        return filterCreationDto;
    }

    @Override
    public final FilterDto getContactFilter(ProjectDto project) {
        log.info("Getting contact filter for project {}", project);
        return this.getFilterService().getContactFilter(project);
    }

    @Override
    public List<Map<String, Object>> getFilters(ProjectDto project) {

        List<Map<String, Object>> filterList = this.getFilterService().getFilters(project);

        return filterList;
    }
}
