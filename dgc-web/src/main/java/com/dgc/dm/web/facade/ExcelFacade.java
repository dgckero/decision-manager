/*
  @author david
 */

package com.dgc.dm.web.facade;

import com.dgc.dm.core.dto.ProjectDto;
import org.springframework.web.multipart.MultipartFile;

public interface ExcelFacade {
    ProjectDto processExcel (MultipartFile file, String project);

    ProjectDto processExcel (MultipartFile file, Integer projectId) throws Exception;

    void processExcel (MultipartFile file, ProjectDto project) throws Exception;
}
