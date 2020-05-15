package com.dgc.dm.core.service.bpmn;

import com.dgc.dm.core.dto.FilterDto;
import com.dgc.dm.core.dto.ProjectDto;
import com.dgc.dm.core.service.db.FilterService;
import com.dgc.dm.core.service.db.RowDataService;
import com.dgc.dm.core.service.email.EmailService;
import org.camunda.bpm.model.dmn.DmnModelException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

class BPMNServerImplTest {

    @Mock
    private EmailService mockEmailService;
    @Mock
    private RowDataService mockRowDataService;
    @Mock
    private FilterService mockFilterService;

    @InjectMocks
    private BPMNServerImpl bpmnServerImplUnderTest;

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

    ProjectDto project = new ProjectDto(0, "name", "rowDataTableName", "emailTemplate", dmnTestFile.getBytes());
    final List<FilterDto> activeFilters = Arrays.asList(new FilterDto(0, "name", "filterClass", "value", false, false, project));
    final FilterDto filterDto = new FilterDto(0, "name", "filterClass", "value", true, true, project);

    @BeforeEach
    void setUp() {
        initMocks(this);
    }

    @Test
    void testCreateAndRunDMN_doNotSendEmail() throws Exception {
        // Setup
        when(mockRowDataService.getRowData(project)).thenReturn(Arrays.asList(new HashMap<String, Object>() {
            {
                put("name", "value");
            }
        }));

        // Configure FilterService.getContactFilter(...).
        when(mockFilterService.getContactFilter(project)).thenReturn(filterDto);

        // Run the test
        final List<Map<String, Object>> result = bpmnServerImplUnderTest.createAndRunDMN(project, activeFilters, false);

        // Verify the results
        verify(mockEmailService, never()).sendAsynchronousMail("toEmail", project);
    }

    @Test
    void testCreateAndRunDMN_sendEmail() throws Exception {
        // Setup
        when(mockRowDataService.getRowData(project)).thenReturn(Arrays.asList(new HashMap<String, Object>() {
            {
                put("name", "value");
            }
        }));

        // Configure FilterService.getContactFilter(...).
        when(mockFilterService.getContactFilter(project)).thenReturn(filterDto);
        doNothing().when(mockEmailService).sendAsynchronousMail("toEmail", project);
        // Run the test
        final List<Map<String, Object>> result = bpmnServerImplUnderTest.createAndRunDMN(project, activeFilters, true);

        // Verify the results
        verify(mockEmailService).sendAsynchronousMail("value", project);
    }

    @Test
    void testValidateDmn() {
        // Run the test
        bpmnServerImplUnderTest.validateDmn(dmnTestFile.getBytes());
        // Verify the results
        assertTrue(true);

    }

    @Test
    void testValidateDmn_throwsDmnModelException() {
        // Run the test
        assertThrows(DmnModelException.class, () -> {
            bpmnServerImplUnderTest.validateDmn("content".getBytes());
        });

    }

    @Test
    void testExecuteDmn() {
        // Setup
        when(mockRowDataService.getRowData(project)).thenReturn(Arrays.asList(new HashMap<String, Object>() {
            {
                put("name", "value");
            }
        }));

        // Configure FilterService.getContactFilter(...).
        when(mockFilterService.getContactFilter(project)).thenReturn(filterDto);
        doNothing().when(mockEmailService).sendAsynchronousMail("value", project);
        // Run the test
        final List<Map<String, Object>> result = bpmnServerImplUnderTest.executeDmn(project);

        // Verify the results
        verify(mockEmailService).sendAsynchronousMail("value", project);
    }
}
