/*
  @author david
 */

package com.dgc.dm.core.service.bpmn;

import com.dgc.dm.core.dto.FilterDto;
import com.dgc.dm.core.dto.ProjectDto;

import java.util.List;
import java.util.Map;

public interface BPMNServer {
    List<Map<String, Object>> createAndRunDMN(ProjectDto project, List<FilterDto> activeFilters, boolean sendMail) throws Exception;

    void validateDmn(byte[] bytes);

    List<Map<String, Object>> executeDmn(ProjectDto selectedProject);
}
