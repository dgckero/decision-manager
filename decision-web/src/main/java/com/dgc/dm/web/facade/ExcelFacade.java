/*
  @author david
 */

package com.dgc.dm.web.facade;

import com.dgc.dm.core.dto.FilterDto;
import com.dgc.dm.core.dto.ProjectDto;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface ExcelFacade {
    String processExcel(MultipartFile file, ProjectDto project, List<Object[]> infoToBePersisted) throws IOException;

    String addInformationToProject(MultipartFile file, ProjectDto project, int rowIdNumber, List<FilterDto> projectFilters, List<Object[]> infoToBePersisted);

    Map<String, Class<?>> getExcelColumnNames(MultipartFile file) throws IOException;

    Map<String, Class<?>> compareExcelColumnNames(MultipartFile file, ProjectDto project, List<FilterDto> projectFilters);

    Map<String, Class<?>> compareExcelColumnNames(Sheet worksheet, MultipartFile file, ProjectDto project, List<FilterDto> projectFilters);
}
