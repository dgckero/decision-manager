/*
  @author david
 */

package com.dgc.dm.web.facade;

import com.dgc.dm.core.dto.ProjectDto;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface ExcelFacade {
    ProjectDto processExcel (MultipartFile file, String project) throws IOException;

    ProjectDto processExcel (MultipartFile file, Integer projectId);

    void processExcel (MultipartFile file, ProjectDto project);
}
