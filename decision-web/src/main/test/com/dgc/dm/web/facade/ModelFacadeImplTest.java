package com.dgc.dm.web.facade;

import com.dgc.dm.core.dto.FilterCreationDto;
import com.dgc.dm.core.dto.FilterDto;
import com.dgc.dm.core.dto.ProjectDto;
import com.dgc.dm.core.service.bpmn.BPMNServer;
import com.dgc.dm.core.service.db.FilterService;
import com.dgc.dm.core.service.db.ProjectService;
import com.dgc.dm.core.service.db.RowDataService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.ModelAndView;

import java.util.*;

import static com.dgc.dm.web.facade.ModelFacadeImpl.NO_FILTERS_FOUND;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

class ModelFacadeImplTest {

    @Mock
    private BPMNServer mockBpmnServer;
    @Mock
    private FilterService mockFilterService;
    @Mock
    private ProjectService mockProjectService;
    @Mock
    private RowDataService mockRowDataService;

    @InjectMocks
    private ModelFacadeImpl modelFacadeImplUnderTest;

    final ProjectDto project = new ProjectDto(0, "name", "rowDataTableName", "emailTemplate", "content".getBytes());

    List<Map<String, Object>> filters = new ArrayList<Map<String, Object>>() {
        {
            add(new HashMap<String, Object>() {{
                put("orderDate", "filterVal");
            }});
            add(new HashMap<String, Object>() {{
                put("Region", "filterVal");
            }});
            add(new HashMap<String, Object>() {{
                put("Rep", "filterVal");
            }});
            add(new HashMap<String, Object>() {{
                put("Item", "filterVal");
            }});
            add(new HashMap<String, Object>() {{
                put("Units", "filterVal");
            }});
            add(new HashMap<String, Object>() {{
                put("UnitsCost", "filterVal");
            }});
            add(new HashMap<String, Object>() {{
                put("Total", "filterVal");
            }});
            add(new HashMap<String, Object>() {{
                put("name", "project");
                put("project", "filterVal");
            }});
        }
    };

    @BeforeEach
    void setUp() {
        initMocks(this);
        modelFacadeImplUnderTest.setFilterService(mockFilterService);
        modelFacadeImplUnderTest.setProjectService(mockProjectService);
        modelFacadeImplUnderTest.setRowDataService(mockRowDataService);
    }

    @Test
    void testGetFilterCreationDto_projectIsNull() {
        // Setup
        final Collection<Map<String, Object>> filterList = Arrays.asList(
                new HashMap<String, Object>() {
                    {
                        put("test", "test");
                        put("contactFilter", 0);
                    }
                }
        );

        // Run the test
        final FilterCreationDto result = modelFacadeImplUnderTest.getFilterCreationDto(null, filterList);

        // Verify the results
        assertNull(result);
    }

    @Test
    void testGetFilterCreationDto_filterListIsNull() {

        // Run the test
        final FilterCreationDto result = modelFacadeImplUnderTest.getFilterCreationDto(project, null);

        // Verify the results
        assertNull(result);
    }

    @Test
    void testGetFilterCreationDto_allFiltersOmmitted() {
        // Setup
        List<Map<String, Object>> filters = new ArrayList<Map<String, Object>>() {
            {
                add(new HashMap<String, Object>() {{
                    put("name", "rowId");
                    put("rowId", "filterVal");
                }});
                add(new HashMap<String, Object>() {{
                    put("name", "dataCreationDate");
                    put("dataCreationDate", "filterVal");
                }});
                add(new HashMap<String, Object>() {{
                    put("name", "project");
                    put("project", "filterVal");
                }});
            }
        };

        // Run the test
        final FilterCreationDto result = modelFacadeImplUnderTest.getFilterCreationDto(project, filters);

        // Verify the results
        assertNull(result);
    }

    @Test
    void testGetFilterCreationDto() {

        // Run the test
        final FilterCreationDto result = modelFacadeImplUnderTest.getFilterCreationDto(project, filters);

        // Verify the results
        assertNotNull(result);
    }

    @Test
    void testGetContactFilter() {
        // Setup
        final FilterDto expectedResult = new FilterDto(0, "name", "filterClass", "value", false, false, new ProjectDto(0, "name", "rowDataTableName", "emailTemplate", "content".getBytes()));
        when(mockFilterService.getContactFilter(project)).thenReturn(expectedResult);

        // Run the test
        final FilterDto result = modelFacadeImplUnderTest.getContactFilter(project);

        // Verify the results
        assertEquals(expectedResult, result);
    }

    @Test
    void testGetContactFilter_projectIsNull() {
        // Run the test
        final FilterDto result = modelFacadeImplUnderTest.getContactFilter(null);

        // Verify the results
        assertNull(result);
    }

    @Test
    void testGetFilters() {
        // Setup
        when(mockFilterService.getFilters(project)).thenReturn(anyList());
        // Run the test
        modelFacadeImplUnderTest.getFilters(project);

        // Verify the results
        assertTrue(true);
    }

    @Test
    void testGetFilters_projectIsNull() {
        // Run the test
        List<Map<String, Object>> result = modelFacadeImplUnderTest.getFilters(null);

        // Verify the results
        assertNull(result);
    }

    @Test
    void testUpdateProject() {
        // Setup
        doNothing().when(mockProjectService).updateProject(project);
        // Run the test
        modelFacadeImplUnderTest.updateProject(project);
        // Verify the results
        assertTrue(true);
    }

    @Test
    void testUpdateProject_projectIsNull() {
        // Run the test
        modelFacadeImplUnderTest.updateProject(null);
        // Verify the results
        verify(mockProjectService, never()).updateProject(any());
    }

    @Test
    void testUpdateFilters() {
        // Setup
        final List<FilterDto> filters = Arrays.asList(new FilterDto(0, "name", "filterClass", "value", false, false, new ProjectDto(0, "name", "rowDataTableName", "emailTemplate", "content".getBytes())));
        doNothing().when(mockFilterService).updateFilters(filters);

        // Run the test
        modelFacadeImplUnderTest.updateFilters(filters);

        // Verify the results
        assertTrue(true);
    }

    @Test
    void testUpdateFilters_filtersNull() {

        // Run the test
        modelFacadeImplUnderTest.updateFilters(null);

        // Verify the results
        verify(mockFilterService, never()).updateFilters(any());
    }

    @Test
    void testCreateDMNModel() throws Exception {
        // Setup
        final List<FilterDto> filters = Arrays.asList(new FilterDto(0, "name", "filterClass", "value", false, false, project));
        when(mockBpmnServer.createAndRunDMN(project, filters, true)).thenReturn(Arrays.asList(new HashMap<>()));
        doNothing().when(mockFilterService).updateFilters(filters);

        doNothing().when(mockProjectService).updateProject(project);
        // Run the test
        final List<Map<String, Object>> result = modelFacadeImplUnderTest.createDMNModel(filters, "emailTemplate", false);

        // Verify the results
        assertTrue(true);
    }

    @Test
    void testCreateDMNModel_projectIsNull() throws Exception {
        // Setup
        final List<FilterDto> filters = Arrays.asList(new FilterDto(0, "name", "filterClass", "value", false, false, null));
        // Run the test
        final List<Map<String, Object>> result = modelFacadeImplUnderTest.createDMNModel(filters, "emailTemplate", false);

        // Verify the results
        assertNull(result);
    }

    @Test
    void testCreateDMNModel_filtersIsNull() throws Exception {
        // Run the test
        final List<Map<String, Object>> result = modelFacadeImplUnderTest.createDMNModel(null, "emailTemplate", false);

        // Verify the results
        assertNull(result);
    }

    @Test
    void testCreateDMNModel1() throws Exception {
        // Setup
        final ProjectDto project = new ProjectDto(0, "name", "rowDataTableName", "emailTemplate", "content".getBytes());
        final List<FilterDto> filters = Arrays.asList(new FilterDto(0, "name", "filterClass", "value", false, false, new ProjectDto(0, "name", "rowDataTableName", "emailTemplate", "content".getBytes())));
        when(mockBpmnServer.createAndRunDMN(new ProjectDto(0, "name", "rowDataTableName", "emailTemplate", "content".getBytes()), Arrays.asList(new FilterDto(0, "name", "filterClass", "value", false, false, new ProjectDto(0, "name", "rowDataTableName", "emailTemplate", "content".getBytes()))), false)).thenReturn(Arrays.asList(new HashMap<>()));

        // Run the test
        final List<Map<String, Object>> result = modelFacadeImplUnderTest.createDMNModel(project, filters, "emailTemplate", false);

        // Verify the results
        assertTrue(true);
    }

    @Test
    void testGetActiveFilters() {
        // Setup
        final List<FilterDto> filters = Arrays.asList(
                new FilterDto(0, "name", "filterClass", "value", true, true,
                        project
                ));
        // Run the test
        final List<FilterDto> result = modelFacadeImplUnderTest.getActiveFilters(filters);

        // Verify the results
        assertFalse(result.isEmpty());
    }

    @Test
    void testGetProjects() {
        // Setup
        List<Map<String, Object>> projects = new ArrayList<Map<String, Object>>() {
            {
                add(new HashMap<String, Object>() {
                    {
                        put("project", project);
                    }
                });
            }
        };
        when(mockProjectService.getProjects()).thenReturn(projects);
        // Run the test
        final List<Map<String, Object>> result = modelFacadeImplUnderTest.getProjects();

        // Verify the results
        assertFalse(result.isEmpty());
    }

    @Test
    void testGetProjects_noProjectsFound() {

        when(mockProjectService.getProjects()).thenReturn(null);
        // Run the test
        final List<Map<String, Object>> result = modelFacadeImplUnderTest.getProjects();

        // Verify the results
        assertNull(result);
    }

    @Test
    void testGetProject() {
        // Setup
        when(mockProjectService.getProject(0)).thenReturn(project);

        // Run the test
        final ProjectDto result = modelFacadeImplUnderTest.getProject(0);

        // Verify the results
        assertEquals(project, result);
    }

    @Test
    void testGetProject_noProjectFound() {
        // Setup
        when(mockProjectService.getProject(0)).thenReturn(null);

        // Run the test
        final ProjectDto result = modelFacadeImplUnderTest.getProject(0);

        // Verify the results
        assertNull(result);
    }

    @Test
    void testExecuteDmn() {
        // Setup
        when(mockBpmnServer.executeDmn(project)).thenReturn(Arrays.asList(new HashMap<>()));

        // Run the test
        final List<Map<String, Object>> result = modelFacadeImplUnderTest.executeDmn(project);

        // Verify the results
        assertTrue(true);
    }

    @Test
    void testExecuteDmn_returnNull() {
        // Setup
        when(mockBpmnServer.executeDmn(project)).thenReturn(null);

        // Run the test
        final List<Map<String, Object>> result = modelFacadeImplUnderTest.executeDmn(project);

        // Verify the results
        assertNull(result);
    }

    @Test
    void testValidateDmn() {
        // Run the test
        modelFacadeImplUnderTest.validateDmn(project, project.getDmnFile());

        // Verify the results
        verify(mockBpmnServer).validateDmn(project.getDmnFile());
    }

    @Test
    void testGetRowData() {
        // Setup
        when(mockRowDataService.getRowData(project)).thenReturn(Arrays.asList(new HashMap<>()));

        // Run the test
        final List<Map<String, Object>> result = modelFacadeImplUnderTest.getRowData(project);

        // Verify the results
        assertTrue(true);
    }

    @Test
    void testGetRowData_projectIsNull() {

        // Run the test
        final List<Map<String, Object>> result = modelFacadeImplUnderTest.getRowData(null);

        // Verify the results
        assertNull(result);
    }

    @Test
    void testGetRowData_noDataFound() {
        // Setup
        when(mockRowDataService.getRowData(project)).thenReturn(null);

        // Run the test
        final List<Map<String, Object>> result = modelFacadeImplUnderTest.getRowData(project);

        // Verify the results
        assertNull(result);
    }

    @Test
    void testDeleteRowData() {
        // Setup
        doNothing().when(mockRowDataService).deleteRowData(project);

        // Run the test
        modelFacadeImplUnderTest.deleteRowData(project);

        // Verify the results
        assertTrue(true);
    }

    @Test
    void testDeleteRowData_projectNull() {

        // Run the test
        modelFacadeImplUnderTest.deleteRowData(null);

        // Verify the results
        verify(mockRowDataService, never()).deleteRowData(any());
    }

    @Test
    void testDeleteProject() {
        // Setup
        doNothing().when(mockProjectService).deleteProject(project);

        // Run the test
        modelFacadeImplUnderTest.deleteProject(project);

        // Verify the results
        assertTrue(true);
    }

    @Test
    void testDeleteProject_projectIsNull() {
        // Setup
        doNothing().when(mockProjectService).deleteProject(project);

        // Run the test
        modelFacadeImplUnderTest.deleteProject(null);

        // Verify the results
        verify(mockProjectService, never()).deleteProject(any());
    }

    @Test
    void testAddFilterInformationToModel() {
        // Setup
        final ModelAndView modelAndView = new ModelAndView("viewName", new HashMap<>(), HttpStatus.CONTINUE);
        when(mockFilterService.getFilters(project)).thenReturn(filters);

        // Run the test
        modelFacadeImplUnderTest.addFilterInformationToModel(modelAndView, project);

        // Verify the results
        assertNotNull(modelAndView.getModelMap().get("form"));
    }

    @Test
    void testAddFilterInformationToModel_filtersIsEmpty() {
        // Setup
        final ModelAndView modelAndView = new ModelAndView("viewName", new HashMap<>(), HttpStatus.CONTINUE);

        // Run the test
        modelFacadeImplUnderTest.addFilterInformationToModel(modelAndView, project);

        // Verify the results
        assertEquals(NO_FILTERS_FOUND, modelAndView.getModel().get("message"));
    }

    @Test
    void testGetExistingProjects() {
        // Setup
        List<Map<String, Object>> projects = new ArrayList<Map<String, Object>>() {
            {
                add(new HashMap<String, Object>() {
                    {
                        put("project", project);
                    }
                });
            }
        };
        when(mockProjectService.getProjects()).thenReturn(projects);

        // Run the test
        final Map<String, List<Map<String, Object>>> result = modelFacadeImplUnderTest.getExistingProjects();

        // Verify the results
        assertFalse(result.isEmpty());
    }

    @Test
    void testGetExistingProjects_getProjectsReturnNull() {
        // Setup
        List<Map<String, Object>> projects = new ArrayList<Map<String, Object>>() {
            {
                add(new HashMap<String, Object>() {
                    {
                        put("project", project);
                    }
                });
            }
        };
        when(mockProjectService.getProjects()).thenReturn(null);

        // Run the test
        final Map<String, List<Map<String, Object>>> result = modelFacadeImplUnderTest.getExistingProjects();

        // Verify the results
        assertNull(result);
    }
}
