/*
  @author david
 */

package com.dgc.dm.core.service.bpmn;

import com.dgc.dm.core.dto.FilterDto;
import com.dgc.dm.core.dto.ProjectDto;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface BPMNServer {

    List<Map<String, Object>> createAndRunDMN (ProjectDto project, List<FilterDto> activeFilters, Boolean sendMail) throws IOException;

    void validateDmn (byte[] bytes);

    List<Map<String, Object>> executeDmn (ProjectDto selectedProject);

}
