/*
  @author david
 */

package com.dgc.dm.core.bpmn;

import com.dgc.dm.core.dto.FilterDto;

import java.util.List;
import java.util.Map;

public interface BPMNServer {
    List<Map<String, Object>> createBPMNModel(List<FilterDto> activeFilters, boolean evaluateDecisionTable) throws Exception;

    void createBPMNModel(boolean evaluateDecisionTable);
}
