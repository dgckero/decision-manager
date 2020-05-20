package com.dgc.dm.web.controller;

import com.dgc.dm.core.dto.FilterCreationDto;
import com.dgc.dm.core.dto.FilterDto;
import com.dgc.dm.core.dto.ProjectDto;
import com.dgc.dm.core.exception.DecisionException;
import com.dgc.dm.web.facade.ExcelFacade;
import com.dgc.dm.web.facade.ModelFacade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

class ProjectControllerImplTest {

    @Mock
    private ModelFacade mockModelFacade;

    @Mock
    private ExcelFacade mockExcelFacade;

    @InjectMocks
    private ProjectControllerImpl projectControllerImplUnderTest;

    String dmnTestFile = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
            "<definitions id=\"definition-0\" name=\"definition-name\" namespace=\"http://camunda.org/schema/1.0/dmn\" xmlns=\"http://www.omg.org/spec/DMN/20151101/dmn.xsd\">\n" +
            "  <decision id=\"decision-0\" name=\"decision-name\">\n" +
            "    <decisionTable hitPolicy=\"COLLECT\" id=\"decisionTable-name\">\n" +
            "      <input id=\"input_62182179-95ab-49fd-adc9-e1b7d5850153\">\n" +
            "        <inputExpression id=\"inputExpression_name\" typeRef=\"filterclass\">\n" +
            "          <text>name</text>\n" +
            "        </inputExpression>\n" +
            "      </input>\n" +
            "      <output id=\"output1\" label=\"rule matched?\" typeRef=\"string\"/>\n" +
            "      <rule id=\"rule_f014d999-6bb9-4b93-b3f8-1914ce4704b3\">\n" +
            "        <inputEntry id=\"inputEntry_1feab26d-8e9b-4943-8667-7aa42faa1f92\" label=\"name\">\n" +
            "          <text>\"value\"</text>\n" +
            "        </inputEntry>\n" +
            "        <outputEntry id=\"Accepted\" label=\"Accepted_sendEMail\">\n" +
            "          <text>\"Accepted_sendEMail\"</text>\n" +
            "        </outputEntry>\n" +
            "      </rule>\n" +
            "    </decisionTable>\n" +
            "  </decision>\n" +
            "</definitions>";

    ProjectDto projectDto = new ProjectDto(0, "name", "rowDataTableName", "emailTemplate", dmnTestFile.getBytes());


    @BeforeEach
    void setUp() {
        initMocks(this);
    }

    @Test
    void testGetProjects_null() {
        // setup
        when(mockModelFacade.getExistingProjects()).thenReturn(null);
        // Run the test
        assertThrows(DecisionException.class, () -> {
            projectControllerImplUnderTest.getProjects();
        });
    }

    @Test
    void testGetProjects() {
        // setup
        Map<String, List<Map<String, Object>>> projects = new HashMap<String, List<Map<String, Object>>>() {
            {
                put("test", new ArrayList<>());
                put("test2", new ArrayList<>());
                put("test3", new ArrayList<>());
            }
        };
        when(mockModelFacade.getExistingProjects()).thenReturn(projects);
        // Run the test
        final ModelAndView result = projectControllerImplUnderTest.getProjects();

        // Verify the results
        assertEquals("selectProject", result.getViewName());
    }

    @Test
    void testCreateProject_emptyFile() throws Exception {
        // Setup
        String name = "file.txt";
        String originalFileName = "file.txt";
        String contentType = "text/plain";
        byte[] content = null;
        MultipartFile mockMultipartFile = new MockMultipartFile(name,
                originalFileName, contentType, content);

        // Run the test
        assertThrows(DecisionException.class, () -> {
            projectControllerImplUnderTest.createProject("projectName", mockMultipartFile);
        });
    }

    @Test
    void testCreateProject() throws Exception {
        // Setup
        String name = "file.txt";
        String originalFileName = "file.txt";
        String contentType = "text/plain";
        byte[] content = "Hello World!".getBytes();
        MultipartFile mockMultipartFile = new MockMultipartFile(name,
                originalFileName, contentType, content);
        when(mockExcelFacade.processExcel(mockMultipartFile, "projectName")).thenReturn(projectDto);

        // Run the test
        final ModelAndView result = projectControllerImplUnderTest.createProject("projectName", mockMultipartFile);

        // Verify the results
        assertEquals(result.getModel().get("project"), projectDto);
    }

    @Test
    void testCreateProject_throwsDecisionException() throws Exception {
        // Setup
        String name = "file.txt";
        String originalFileName = "file.txt";
        String contentType = "text/plain";
        byte[] content = "Hello World!".getBytes();
        MultipartFile mockMultipartFile = new MockMultipartFile(name,
                originalFileName, contentType, content);
        when(mockExcelFacade.processExcel(mockMultipartFile, "projectName")).thenReturn(null);

        // Run the test
        assertThrows(DecisionException.class, () -> {
            projectControllerImplUnderTest.createProject("projectName", mockMultipartFile);
        });
    }

    @Test
    void testAddInformationToProject() {
        // Setup
        String name = "file.txt";
        String originalFileName = "file.txt";
        String contentType = "text/plain";
        byte[] content = "Hello World!".getBytes();
        MultipartFile mockMultipartFile = new MockMultipartFile(name,
                originalFileName, contentType, content);
        when(mockExcelFacade.processExcel(mockMultipartFile, 0)).thenReturn(projectDto);
        // Run the test
        final ModelAndView result = projectControllerImplUnderTest.addInformationToProject(0, mockMultipartFile);

        // Verify the results
        assertEquals(result.getModel().get("project"), projectDto);
    }

    @Test
    void testAddInformationToProject_ThrowsDecisionException() {
        // Setup
        String name = "file.txt";
        String originalFileName = "file.txt";
        String contentType = "text/plain";
        byte[] content = null;
        MultipartFile mockMultipartFile = new MockMultipartFile(name,
                originalFileName, contentType, content);

        // Run the test
        assertThrows(DecisionException.class, () -> {
            projectControllerImplUnderTest.addInformationToProject(0, mockMultipartFile);
        });
    }

    @Test
    void testAddInformationToProject_ThrowsDecisionException2() {
        // Setup
        String name = "file.txt";
        String originalFileName = "file.txt";
        String contentType = "text/plain";
        byte[] content = "test".getBytes();
        MultipartFile mockMultipartFile = new MockMultipartFile(name,
                originalFileName, contentType, content);

        when(mockExcelFacade.processExcel(mockMultipartFile, 0)).thenReturn(null);

        // Run the test
        assertThrows(DecisionException.class, () -> {
            projectControllerImplUnderTest.addInformationToProject(0, mockMultipartFile);
        });
    }

    @Test
    void testAddInformationToProject_ThrowsDecisionException3() {
        // Setup
        String name = "file.txt";
        String originalFileName = "file.txt";
        String contentType = "text/plain";
        byte[] content = "test".getBytes();
        MultipartFile mockMultipartFile = new MockMultipartFile(name,
                originalFileName, contentType, content);

        when(mockExcelFacade.processExcel(mockMultipartFile, 0)).thenThrow(NullPointerException.class);

        // Run the test
        assertThrows(DecisionException.class, () -> {
            projectControllerImplUnderTest.addInformationToProject(0, mockMultipartFile);
        });
    }

    @Test
    void testGetProjectFilters_throwsDecisionException() {
        // Setup
        when(mockModelFacade.getProject(0)).thenReturn(null);

        // Run the test
        assertThrows(DecisionException.class, () -> {
            projectControllerImplUnderTest.getProjectFilters(0);
        });
    }

    @Test
    void testGetProjectFilters() {
        // Setup
        when(mockModelFacade.getProject(0)).thenReturn(projectDto);
        ModelAndView modelAndView = new ModelAndView(CommonController.FILTERS_VIEW);
        doNothing().when(mockModelFacade).addFilterInformationToModel(modelAndView, projectDto);

        // Run the test
        ModelAndView result = projectControllerImplUnderTest.getProjectFilters(0);
        assertEquals(result.getModel().get("project"), projectDto);
    }

    @Test
    void testEditEmailTemplate() {
        // Setup
        when(mockModelFacade.getProject(0)).thenReturn(projectDto);
        doNothing().when(mockModelFacade).updateProject(projectDto);
        String testEmailTemplate = "TestEmailTemplate";
        // Run the test
        final ModelAndView result = projectControllerImplUnderTest.editEmailTemplate(0, testEmailTemplate);

        // Verify the results
        assertEquals(projectDto.getEmailTemplate(), testEmailTemplate);
    }

    @Test
    void testGetFilteredResults() {
        // Setup
        List<Map<String, Object>> result = new ArrayList<>();
        when(mockModelFacade.getProject(0)).thenReturn(projectDto);
        when(mockModelFacade.executeDmn(projectDto)).thenReturn(result);

        // Run the test
        ModelAndView modelAndView = projectControllerImplUnderTest.getFilteredResults(0);

        // Verify the results
        assertEquals(modelAndView.getModelMap().getAttribute("form"), result);
    }

    @Test
    void testGetFilteredResults_throwsDecisionException() {
        // Setup
        projectDto.setDmnFile(null);
        when(mockModelFacade.getProject(0)).thenReturn(projectDto);

        // Run the test
        assertThrows(DecisionException.class, () -> {
            projectControllerImplUnderTest.getFilteredResults(0);
        });
    }

    @Test
    void testGetFilteredResults_throwsDecisionException2() {
        // Setup
        List<Map<String, Object>> result = new ArrayList<>();
        when(mockModelFacade.getProject(0)).thenReturn(projectDto);
        when(mockModelFacade.executeDmn(projectDto)).thenReturn(null);

        // Run the test
        assertThrows(DecisionException.class, () -> {
            projectControllerImplUnderTest.getFilteredResults(0);
        });
    }

    @Test
    void testGetProjectDmn_throwsDecisionException() {
        // Setup
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(mockModelFacade.getProject(0)).thenReturn(projectDto);

        // Run the test
        assertThrows(DecisionException.class, () -> {
            projectControllerImplUnderTest.getProjectDmn(0, response);
        });
    }

    @Test
    void testEditDmn() {
        // Setup
        String name = "file.txt";
        String originalFileName = "file.txt";
        String contentType = "text/plain";
        byte[] content = "Hello World!".getBytes();
        MultipartFile mockMultipartFile = new MockMultipartFile(name,
                originalFileName, contentType, content);
        List<Map<String, Object>> result = new ArrayList<>();
        try {
            when(mockModelFacade.getProject(0)).thenReturn(projectDto);
            doNothing().when(mockModelFacade).validateDmn(projectDto, mockMultipartFile.getBytes());
            when(mockModelFacade.executeDmn(projectDto)).thenReturn(result);
            doNothing().when(mockModelFacade).updateProject(projectDto);

            // Run the test
            final ModelAndView modelAndView = projectControllerImplUnderTest.editDmn(0, mockMultipartFile);

            // Verify the results
            assertTrue(true);
        } catch (IOException e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }

    @Test
    void testEditDmn_throwsDecisionException() {
        // Setup
        String name = "file.txt";
        String originalFileName = "file.txt";
        String contentType = "text/plain";
        byte[] content = "Hello World!".getBytes();
        MultipartFile mockMultipartFile = new MockMultipartFile(name,
                originalFileName, contentType, content);
        try {
            when(mockModelFacade.getProject(0)).thenReturn(projectDto);
            doNothing().when(mockModelFacade).validateDmn(projectDto, mockMultipartFile.getBytes());
            when(mockModelFacade.executeDmn(projectDto)).thenReturn(null);
            doNothing().when(mockModelFacade).updateProject(projectDto);

            assertThrows(DecisionException.class, () -> {
                projectControllerImplUnderTest.editDmn(0, mockMultipartFile);
            });

        } catch (IOException e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }

    @Test
    void testEditDmn_throwsDecisionException2() {
        // Setup
        String name = "file.txt";
        String originalFileName = "file.txt";
        String contentType = "text/plain";
        byte[] content = null;
        MultipartFile mockMultipartFile = new MockMultipartFile(name,
                originalFileName, contentType, content);
        when(mockModelFacade.getProject(0)).thenReturn(projectDto);

        // Run the test
        assertThrows(DecisionException.class, () -> {
            projectControllerImplUnderTest.editDmn(0, mockMultipartFile);
        });

    }

    @Test
    void testGetAllRegisters() {
        // Setup
        when(mockModelFacade.getProject(0)).thenReturn(projectDto);
        when(mockModelFacade.getRowData(projectDto)).thenReturn(new ArrayList<>());

        // Run the test
        final ModelAndView result = projectControllerImplUnderTest.getAllRegisters(0);

        // Verify the results
        assertTrue(true);
    }

    @Test
    void testGetAllRegisters_throwsDecisionException() {
        // Setup
        when(mockModelFacade.getProject(0)).thenReturn(projectDto);
        when(mockModelFacade.getRowData(projectDto)).thenReturn(null);

        // Run the test
        assertThrows(DecisionException.class, () -> {
            projectControllerImplUnderTest.getAllRegisters(0);
        });
    }

    @Test
    void testDeleteRegisters() {
        // Setup
        when(mockModelFacade.getProject(0)).thenReturn(projectDto);
        doNothing().when(mockModelFacade).deleteRowData(projectDto);
        // Run the test
        final ModelAndView result = projectControllerImplUnderTest.deleteRegisters(0);

        // Verify the results
        assertTrue(true);
    }

    @Test
    void testDeleteProject() {
        // Setup
        when(mockModelFacade.getProject(0)).thenReturn(projectDto);
        doNothing().when(mockModelFacade).deleteProject(projectDto);
        // Run the test
        final ModelAndView result = projectControllerImplUnderTest.deleteProject(0);

        // Verify the results
        assertTrue(true);
    }

    @Test
    void testAddFilters() {
        // Setup
        final FilterCreationDto form = new FilterCreationDto(Arrays.asList(new FilterDto(0, "name", "filterClass", "value", false, false, new ProjectDto(0, "name", "rowDataTableName", "emailTemplate", "content".getBytes()))));
        final HttpServletRequest request = null;
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>() {
            {
                add(new HashMap<String, Object>() {{
                    put("test", "test");
                }});
            }
        };
        try {
            when(mockModelFacade.createDMNModel(form.getFilters().get(0).getProject(), form.getFilters(), null, false)).thenReturn(result);
            // Run the test
            projectControllerImplUnderTest.addFilters("emailTemplate", false, form, request);

            // Verify the results
            assertTrue(true);
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }

    @Test
    void testAddFilters_throwsDecisionException() {
        // Setup
        final FilterCreationDto form = new FilterCreationDto(new ArrayList<>());

        // Run the test
        assertThrows(DecisionException.class, () -> {
            projectControllerImplUnderTest.addFilters("emailTemplate", false, form, null);
        });
    }

    @Test
    void testAddFilters_throwsDecisionException2() {
        // Setup
        final FilterCreationDto form = new FilterCreationDto(Arrays.asList(new FilterDto(0, "name", "filterClass", "value", false, false, new ProjectDto(0, "name", "rowDataTableName", "emailTemplate", "content".getBytes()))));

        try {
            when(mockModelFacade.createDMNModel(form.getFilters().get(0).getProject(), form.getFilters(), null, false)).thenThrow(Exception.class);
            // Run the test
            assertThrows(DecisionException.class, () -> {
                projectControllerImplUnderTest.addFilters("emailTemplate", false, form, null);
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    void testAddFilters_throwsDecisionException3() {
        // Setup
        final FilterCreationDto form = new FilterCreationDto(Arrays.asList(new FilterDto(0, "name", "filterClass", "value", false, false, new ProjectDto(0, "name", "rowDataTableName", "emailTemplate", "content".getBytes()))));
        final HttpServletRequest request = null;

        try {
            when(mockModelFacade.createDMNModel(form.getFilters().get(0).getProject(), form.getFilters(), null, false)).thenReturn(new ArrayList<>());
            // Run the test
            assertThrows(DecisionException.class, () -> {
                projectControllerImplUnderTest.addFilters("emailTemplate", false, form, request);
            });
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }

    @Test
    void testAddFilters_throwsDecisionException4() {
        // Setup
        final FilterCreationDto form = new FilterCreationDto(Arrays.asList(new FilterDto(0, "name", "filterClass", "value", false, false, null)));

        // Run the test
        assertThrows(DecisionException.class, () -> {
            projectControllerImplUnderTest.addFilters("emailTemplate", false, form, null);
        });

    }

    @Test
    void testGetProject() {
        // Setup
        when(mockModelFacade.getProject(0)).thenReturn(projectDto);
        // Run the test
        final ModelAndView result = projectControllerImplUnderTest.getProject(0);

        // Verify the results
        assertTrue(true);
    }

    @Test
    void testGetProject_throwsDecisionException() {

        // Run the test
        assertThrows(DecisionException.class, () -> {
            final ModelAndView result = projectControllerImplUnderTest.getProject(null);
        });

    }
}
