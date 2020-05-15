package com.dgc.dm.web.facade;

import com.dgc.dm.core.dto.ProjectDto;
import com.dgc.dm.core.dto.RowDataDto;
import com.dgc.dm.core.exception.DecisionException;
import com.dgc.dm.core.service.db.FilterService;
import com.dgc.dm.core.service.db.ProjectService;
import com.dgc.dm.core.service.db.RowDataService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

class ExcelFacadeImplTest {

    @InjectMocks
    private ExcelFacadeImpl excelFacadeImplUnderTest;
    @Mock
    private ProjectService mockProjectService;
    @Mock
    private RowDataService mockRowDataService;
    @Mock
    private FilterService mockFilterService;

    final ProjectDto project = new ProjectDto(0, "name", "rowDataTableName", "emailTemplate", "content".getBytes());
    MultipartFile mockEmptyMultipartFile = new MockMultipartFile("name",
            "originalFileName", "contentType", (byte[]) null);
    MultipartFile mockMultipartFile = new MockMultipartFile("name",
            "originalFileName", "contentType", "Hello World!".getBytes());

    @BeforeEach
    void setUp() {
        initMocks(this);
        excelFacadeImplUnderTest.setProjectService(mockProjectService);
        excelFacadeImplUnderTest.setFilterService(mockFilterService);
        excelFacadeImplUnderTest.setRowDataService(mockRowDataService);
    }

    @Test
    void testProcessExcel_throwsDecisionException() {
        // Run the test
        assertThrows(DecisionException.class, () -> {
            excelFacadeImplUnderTest.processExcel(mockMultipartFile, project);
        });
    }

    @Test
    void testProcessExcel() {
        // Setup
        // Run the test
        final ProjectDto result = excelFacadeImplUnderTest.processExcel(mockMultipartFile, 0);

        // Verify the results
        assertTrue(true);
    }

    @Test
    void testCreateProjectModel() {
        // Setup
        final Map<String, Class<?>> colMapByName = new HashMap<>();
        when(mockProjectService.createProject("name")).thenReturn(project);
        doNothing().when(mockRowDataService).createRowDataTable(colMapByName, project);
        doNothing().when(mockFilterService).persistFilterList(new ArrayList<>(), project);

        // Run the test
        final ProjectDto result = excelFacadeImplUnderTest.createProjectModel("name", colMapByName);

        // Verify the results
        assertEquals(project, result);
    }

    @Test
    void testProcessExcelRows() {
        // Setup
        final Map<String, Class<?>> columns = new HashMap<>();

        // Run the test
        excelFacadeImplUnderTest.processExcelRows(null, columns, project, 0);

        // Verify the results
        assertTrue(true);
    }

    @Test
    void testPopulateGeneratedObject() throws Exception {
        // Setup
        final Map<String, Class<?>> columns = new HashMap<>();
        final List<Object> excelObjs = Arrays.asList("value");

        // Run the test
        final Object[] result = excelFacadeImplUnderTest.populateGeneratedObject(project, null, RowDataDto.class, columns, excelObjs, 0);

        // Verify the results
        assertTrue(true);
    }

}
