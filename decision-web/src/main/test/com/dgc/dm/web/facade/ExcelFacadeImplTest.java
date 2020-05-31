package com.dgc.dm.web.facade;

import com.dgc.dm.core.dto.ProjectDto;
import com.dgc.dm.core.dto.RowDataDto;
import com.dgc.dm.core.exception.DecisionException;
import com.dgc.dm.core.service.db.FilterService;
import com.dgc.dm.core.service.db.ProjectService;
import com.dgc.dm.core.service.db.RowDataService;
import org.apache.commons.io.IOUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.hibernate.exception.GenericJDBCException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import org.sqlite.SQLiteErrorCode;
import org.sqlite.SQLiteException;

import javax.persistence.PersistenceException;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
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
    @Mock
    private Sheet mockWorksheet;
    @Mock
    private Row mockRow;

    final ProjectDto project = new ProjectDto(0, "name", "rowDataTableName", "emailTemplate", "content".getBytes());
    MultipartFile mockEmptyMultipartFile = new MockMultipartFile("name",
            "originalFileName", "contentType", (byte[]) null);
    MultipartFile mockMultipartFile = new MockMultipartFile("name",
            "originalFileName", "contentType", "Hello World!".getBytes());
    final Map<String, Class<?>> columns = new HashMap<String, Class<?>>() {
        {
            put(new Date().toString(), Date.class);
            put("stringColumn", String.class);
            put("numberColumn", Double.class);
            put("test@test.com", String.class);
            put(new Date().toString(), String.class);
        }
    };

    File excel = new File("src/main/test/com/dgc/dm/web/facade/SampleData.xlsx");
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
                put("project", "filterVal");
            }});
        }
    };


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

        try {
            project.setName("name_" + Math.random());
            MultipartFile mockExcelMultipartFile = new MockMultipartFile("name",
                    "originalFileName", "contentType", IOUtils.toByteArray(excel.toURI()));
            when(mockProjectService.createProject(project.getName())).thenReturn(project);
            doNothing().when(mockRowDataService).createRowDataTable(columns, project);
            doNothing().when(mockFilterService).persistFilterList(new ArrayList<>(), project);

            // Run the test
            final ProjectDto result = excelFacadeImplUnderTest.processExcel(mockExcelMultipartFile, project.getName());

            // Verify the results
            assertTrue(true);

        } catch (IOException e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }

    @Test
    void testProcessExcel_byName_throwsDecisionException() {
        // Setup

        try {
            String projectName = "name_" + Math.random();
            project.setName(projectName);
            MultipartFile mockExcelMultipartFile = new MockMultipartFile("name",
                    "originalFileName", "contentType", IOUtils.toByteArray(excel.toURI()));

            SQLException sqlException = new SQLiteException(null, SQLiteErrorCode.SQLITE_CONSTRAINT);
            PersistenceException ex = new PersistenceException(null, new GenericJDBCException(null, sqlException, null));

            when(mockProjectService.createProject(project.getName())).thenThrow(ex);
            doNothing().when(mockRowDataService).createRowDataTable(columns, project);
            doNothing().when(mockFilterService).persistFilterList(new ArrayList<>(), project);

            // Run the test
            assertThrows(DecisionException.class, () -> {
                excelFacadeImplUnderTest.processExcel(mockExcelMultipartFile, projectName);
            });

        } catch (IOException e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }


    @Test
    void testProcessExcel_byProjectId() {
        // Setup

        try {
            project.setName("name_" + Math.random());
            MultipartFile mockExcelMultipartFile = new MockMultipartFile("name",
                    "originalFileName", "contentType", IOUtils.toByteArray(excel.toURI()));
            when(mockProjectService.getProject(0)).thenReturn(project);
            when(mockFilterService.getFilters(project)).thenReturn(filters);
            // Run the test
            final ProjectDto result = excelFacadeImplUnderTest.processExcel(mockExcelMultipartFile, 0);

            // Verify the results
            assertTrue(true);

        } catch (IOException e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }

    @Test
    void testProcessExcel_projectNotFound() {
        try {
            // Setup
            MultipartFile mockExcelMultipartFile = new MockMultipartFile("name",
                    "originalFileName", "contentType", IOUtils.toByteArray(excel.toURI()));
            when(mockProjectService.getProject(0)).thenReturn(null);
            // Run the test
            final ProjectDto result = excelFacadeImplUnderTest.processExcel(mockExcelMultipartFile, 0);

            // Verify the results
            assertNull(result);

        } catch (IOException e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }

    @Test
    void testProcessExcel_NoFiltersFound() {
        try {
            // Setup
            MultipartFile mockExcelMultipartFile = new MockMultipartFile("name",
                    "originalFileName", "contentType", IOUtils.toByteArray(excel.toURI()));
            when(mockProjectService.getProject(0)).thenReturn(project);
            when(mockFilterService.getFilters(project)).thenReturn(null);
            // Run the test
            assertThrows(DecisionException.class, () -> {
                final ProjectDto result = excelFacadeImplUnderTest.processExcel(mockExcelMultipartFile, 0);
            });

        } catch (IOException e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }

    @Test
    void testProcessExcel_NoSameColumnNumbers() {
        try {
            // Setup
            List<Map<String, Object>> oneFilter = new ArrayList<Map<String, Object>>() {
                {
                    add(new HashMap<String, Object>() {{
                        put("orderDate", "filterVal");
                    }});
                }
            };

            MultipartFile mockExcelMultipartFile = new MockMultipartFile("name",
                    "originalFileName", "contentType", IOUtils.toByteArray(excel.toURI()));
            when(mockProjectService.getProject(0)).thenReturn(project);
            when(mockFilterService.getFilters(project)).thenReturn(oneFilter);
            // Run the test
            assertThrows(DecisionException.class, () -> {
                final ProjectDto result = excelFacadeImplUnderTest.processExcel(mockExcelMultipartFile, 0);
            });

        } catch (IOException e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }

    @Test
    void testProcessExcel_throwsDecisionException2() {

        // Setup
        when(mockProjectService.getProject(0)).thenReturn(project);
        when(mockFilterService.getFilters(project)).thenReturn(filters);
        // Run the test

        assertThrows(DecisionException.class, () -> {
            final ProjectDto result = excelFacadeImplUnderTest.processExcel(mockEmptyMultipartFile, 0);
        });

    }

    @Test
    void testCreateProjectModel() {
        // Setup
        when(mockProjectService.createProject("name")).thenReturn(project);
        doNothing().when(mockRowDataService).createRowDataTable(columns, project);
        doNothing().when(mockFilterService).persistFilterList(new ArrayList<>(), project);

        // Run the test
        final ProjectDto result = excelFacadeImplUnderTest.createProjectModel("name", columns);

        // Verify the results
        assertEquals(project, result);
    }

    @Test
    void testProcessExcelRows() {
        // Setup
        when(mockWorksheet.getPhysicalNumberOfRows()).thenReturn(2);
        when(mockWorksheet.getRow(2)).thenReturn(mockRow);
        project.setName("name_" + Math.random());

        // Run the test
        excelFacadeImplUnderTest.processExcelRows(mockWorksheet, columns, project, 0);

        // Verify the results
        assertTrue(true);
    }

    @Test
    void testPopulateGeneratedObject() throws Exception {
        // Setup
        final List<Object> excelObjs = Arrays.asList("value");

        // Run the test
        final Object[] result = excelFacadeImplUnderTest.populateGeneratedObject(project, null, RowDataDto.class, columns, excelObjs, 0);

        // Verify the results
        assertTrue(true);
    }

}
