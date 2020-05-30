package com.dgc.dm.web.facade;

import com.dgc.dm.core.dto.FilterDto;
import com.dgc.dm.core.dto.ProjectDto;
import com.dgc.dm.core.dto.RowDataDto;
import com.dgc.dm.core.exception.DecisionException;
import com.dgc.dm.core.service.db.FilterService;
import com.dgc.dm.core.service.db.ProjectService;
import com.dgc.dm.core.service.db.RowDataService;
import org.apache.commons.io.IOUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
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
    List<FilterDto> filters = new ArrayList<FilterDto>() {
        {
            add(FilterDto.builder().name("orderDate").value("filterVal").build());
            add(FilterDto.builder().name("region").value("filterVal").build());
            add(FilterDto.builder().name("Rep").value("filterVal").build());
            add(FilterDto.builder().name("item").value("filterVal").build());
            add(FilterDto.builder().name("Units").value("filterVal").build());
            add(FilterDto.builder().name("UnitsCost").value("filterVal").build());
            add(FilterDto.builder().name("Total").value("filterVal").build());
            add(FilterDto.builder().name("project").value("filterVal").build());
        }
    };


    @BeforeEach
    void setUp() {
        initMocks(this);
    }

    @Test
    void testProcessExcel_throwsIOException() {
        // Run the test
        assertThrows(IOException.class, () -> {
            excelFacadeImplUnderTest.processExcel(mockMultipartFile, project, new ArrayList<>());
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
            final String result = excelFacadeImplUnderTest.processExcel(mockExcelMultipartFile, project, new ArrayList<>());

            // Verify the results
            assertTrue(true);

        } catch (IOException e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }

    @Test
    void testAddInformationToProject() {
        // Setup
        try {
            project.setName("name_" + Math.random());
            MultipartFile mockExcelMultipartFile = new MockMultipartFile("name",
                    "originalFileName", "contentType", IOUtils.toByteArray(excel.toURI()));
            when(mockProjectService.getProject(0)).thenReturn(project);
            when(mockFilterService.getFilters(project)).thenReturn(filters);
            // Run the test
            final String result = excelFacadeImplUnderTest.addInformationToProject(mockExcelMultipartFile, project, 0, filters, new ArrayList<>());

            // Verify the results
            assertTrue(true);

        } catch (IOException e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }

    @Test
    void tesAddInformationToProject_projectNotFound() {
        try {
            // Setup
            MultipartFile mockExcelMultipartFile = new MockMultipartFile("name",
                    "originalFileName", "contentType", IOUtils.toByteArray(excel.toURI()));
            when(mockProjectService.getProject(0)).thenReturn(null);
            // Run the test

            final String result = excelFacadeImplUnderTest.addInformationToProject(mockExcelMultipartFile, null, 0, filters, new ArrayList<>());

            // Verify the results
            assertNull(result);

        } catch (IOException e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }

    @Test
    void testAddInformationToProject_NoFiltersFound() {
        try {
            // Setup
            List<FilterDto> projectFilters = new ArrayList();
            List<Object[]> infoToBePersisted = new ArrayList<>();
            int rowIdNumber = 0;
            MultipartFile mockExcelMultipartFile = new MockMultipartFile("name",
                    "originalFileName", "contentType", IOUtils.toByteArray(excel.toURI()));
            when(mockProjectService.getProject(0)).thenReturn(project);
            when(mockFilterService.getFilters(project)).thenReturn(null);
            // Run the test
            assertThrows(DecisionException.class, () -> {
                excelFacadeImplUnderTest.addInformationToProject(mockExcelMultipartFile, project, rowIdNumber, projectFilters, infoToBePersisted);
            });

        } catch (IOException e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }

    @Test
    void testAddInformationToProject_NoSameColumnNumbers() {
        try {
            // Setup
            List<FilterDto> oneFilter = new ArrayList<FilterDto>() {
                {
                    add(FilterDto.builder().name("orderDate").value("filterVal").build());
                }
            };
            List<FilterDto> projectFilters = new ArrayList();
            List<Object[]> infoToBePersisted = new ArrayList<>();
            int rowIdNumber = 0;

            MultipartFile mockExcelMultipartFile = new MockMultipartFile("name",
                    "originalFileName", "contentType", IOUtils.toByteArray(excel.toURI()));
            when(mockProjectService.getProject(0)).thenReturn(project);
            when(mockFilterService.getFilters(project)).thenReturn(oneFilter);
            // Run the test
            assertThrows(DecisionException.class, () -> {
                excelFacadeImplUnderTest.addInformationToProject(mockExcelMultipartFile, project, rowIdNumber, projectFilters, infoToBePersisted);
            });

        } catch (IOException e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }

    @Test
    void testProcessExcelRows() {
        // Setup
        when(mockWorksheet.getPhysicalNumberOfRows()).thenReturn(2);
        when(mockWorksheet.getRow(2)).thenReturn(mockRow);
        project.setName("name_" + Math.random());
        List<Object[]> infoToBePersisted = new ArrayList<>();
        int rowIdNumber = 0;

        // Run the test
        excelFacadeImplUnderTest.processExcelRows(mockWorksheet, columns, project, rowIdNumber, infoToBePersisted);

        // Verify the results
        assertTrue(true);
    }

    @Test
    void testProcessExcelRows_throwsDecisionException() {
        // Setup
        when(mockWorksheet.getPhysicalNumberOfRows()).thenReturn(2);
        when(mockWorksheet.getRow(2)).thenReturn(mockRow);
        project.setName("name_" + Math.random());
        final Map<String, Class<?>> columns = new HashMap<String, Class<?>>() {
            {
                put("fechaColumn", Date.class);
                put("stringColumn", String.class);
                put("numberColumn", Double.class);
                put("test@test.com", String.class);
                put("15/04/1984", String.class);
                put(null, null);
                put("", String.class);
            }
        };
        List<Object[]> infoToBePersisted = new ArrayList<>();
        int rowIdNumber = 0;

        // Run the test
        assertThrows(DecisionException.class, () -> {
            excelFacadeImplUnderTest.processExcelRows(mockWorksheet, columns, project, rowIdNumber, infoToBePersisted);
        });
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

    @Test
    void testCompareExcelColumnNames_throwsDecisionException() {
        // Setup
        List<FilterDto> projectFilters = new ArrayList();

        // Run the test
        assertThrows(DecisionException.class, () -> {
            excelFacadeImplUnderTest.compareExcelColumnNames(mockMultipartFile, project, projectFilters);
        });
    }

    @Test
    void testGetExcelColumnNames() {
        // Setup
        Map<String, Class<?>> result = null;
        try {
            MultipartFile mockExcelMultipartFile = new MockMultipartFile("name",
                    "originalFileName", "contentType", IOUtils.toByteArray(excel.toURI()));

            // Run the test
            result = excelFacadeImplUnderTest.getExcelColumnNames(mockExcelMultipartFile);

            // Verify the results
            assertNotNull(result);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
