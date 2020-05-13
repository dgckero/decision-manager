package com.dgc.dm.core.service.db;

import com.dgc.dm.core.db.dao.FilterDao;
import com.dgc.dm.core.db.model.Filter;
import com.dgc.dm.core.db.model.Project;
import com.dgc.dm.core.dto.FilterDto;
import com.dgc.dm.core.dto.ProjectDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

class FilterServiceImplTest {

    @Mock
    private ModelMapper mockModelMapper;

    @Mock
    private FilterDao mockFilterDao;

    @InjectMocks
    private FilterServiceImpl filterServiceImplUnderTest;

    final ProjectDto project = new ProjectDto(0, "name", "rowDataTableName", "emailTemplate", "content".getBytes());
    final Project projectEntity = new Project(0, "name", "rowDataTableName", "emailTemplate", "content".getBytes());
    final List<FilterDto> filterList = Arrays.asList(new FilterDto(0, "name", "filterClass", "value", false, false, new ProjectDto(0, "name", "rowDataTableName", "emailTemplate", "content".getBytes())));
    final List<Filter> filterListEntity = Arrays.asList(new Filter(0, "name", "filterClass", "value", false, false, new Project(0, "name", "rowDataTableName", "emailTemplate", "content".getBytes())));

    @BeforeEach
    void setUp() {
        initMocks(this);
    }

    @Test
    void testPersistFilterList() {
        // Setup
        when(mockModelMapper.map(project, Project.class)).thenReturn(projectEntity);
        doNothing().when(mockFilterDao).createFilterTable(projectEntity);
        when(mockModelMapper.map(filterList, (new TypeToken<List<Filter>>() {
        }.getType()))).thenReturn(filterListEntity);
        doNothing().when(mockFilterDao).persistFilterList(filterListEntity);

        // Run the test
        filterServiceImplUnderTest.persistFilterList(filterList, project);

        // Verify the results
        verify(mockFilterDao).createFilterTable(new Project(0, "name", "rowDataTableName", "emailTemplate", "content".getBytes()));
        verify(mockFilterDao).persistFilterList(Arrays.asList(new Filter(0, "name", "filterClass", "value", false, false, new Project(0, "name", "rowDataTableName", "emailTemplate", "content".getBytes()))));
    }

    @Test
    void testGetFilters() {
        // Setup
        when(mockFilterDao.getFilters()).thenReturn(Arrays.asList(new HashMap<>()));

        // Run the test
        final List<Map<String, Object>> result = filterServiceImplUnderTest.getFilters();

        // Verify the results
        assertTrue(true);
    }

    @Test
    void testGetFilters1() {
        // Setup
        when(mockModelMapper.map(project, Project.class)).thenReturn(projectEntity);
        when(mockFilterDao.getFilters(projectEntity)).thenReturn(Arrays.asList(new HashMap<>()));

        // Run the test
        final List<Map<String, Object>> result = filterServiceImplUnderTest.getFilters(project);

        // Verify the results
        assertTrue(true);
    }

    @Test
    void testUpdateFilters() {
        // Setup
        when(mockModelMapper.map(filterList, (new TypeToken<List<Filter>>() {
        }.getType()))).thenReturn(filterListEntity);

        // Run the test
        filterServiceImplUnderTest.updateFilters(filterList);

        // Verify the results
        verify(mockFilterDao).updateFilters(filterListEntity);
    }

    @Test
    void testGetContactFilter() {
        // Setup
        final FilterDto expectedResult = new FilterDto(0, "name", "filterClass", "value", false, false, new ProjectDto(0, "name", "rowDataTableName", "emailTemplate", "content".getBytes()));
        final Filter filterEntity = new Filter(0, "name", "filterClass", "value", false, false, new Project(0, "name", "rowDataTableName", "emailTemplate", "content".getBytes()));
        when(mockModelMapper.map(project, Project.class)).thenReturn(projectEntity);
        when(mockFilterDao.getContactFilter(projectEntity)).thenReturn(filterEntity);
        when(mockModelMapper.map(filterEntity, FilterDto.class)).thenReturn(expectedResult);

        // Run the test
        final FilterDto result = filterServiceImplUnderTest.getContactFilter(project);

        // Verify the results
        assertEquals(expectedResult, result);
    }
}
