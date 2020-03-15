/*
  @author david
 */

package com.dgc.dm.core.bpmn;

import com.dgc.dm.core.dto.FilterDto;

import java.util.List;

public interface BPMNServer {
    void createBPMNModel(List<FilterDto> activeFilters) throws Exception;

    void createBPMNModel();
}
