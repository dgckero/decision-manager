/*
  @author david
 */

package com.dgc.dm.core.bpmn;

import com.dgc.dm.core.dto.FilterDto;
import com.dgc.dm.core.dto.ProjectDto;
import org.camunda.bpm.model.dmn.DmnModelException;

import java.util.List;
import java.util.Map;

public interface BPMNServer {
    List<Map<String, Object>> createBPMNModel(ProjectDto project, List<FilterDto> activeFilters, boolean evaluateDecisionTable, boolean sendMail) throws Exception;

    void createBPMNModel(boolean evaluateDecisionTable);

    void validateDmn(byte[] bytes) throws DmnModelException;

    List<Map<String, Object>> executeDmn(ProjectDto selectedProject);
}
