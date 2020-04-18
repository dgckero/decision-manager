/*
  @author david
 */

package com.dgc.dm.web.service;

import com.dgc.dm.core.dto.ProjectDto;
import org.springframework.web.multipart.MultipartFile;

public interface ExcelFacade {
    ProjectDto processExcel(MultipartFile file, String project);
}
