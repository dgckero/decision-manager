/*
  @author david
 */

package com.dgc.dm.web.controller;

import com.dgc.dm.core.db.model.Project;
import com.dgc.dm.core.db.service.DbServer;
import com.dgc.dm.core.dto.CommonDto;
import com.dgc.dm.core.dto.FilterCreationDto;
import com.dgc.dm.core.dto.FilterDto;
import com.dgc.dm.core.dto.ProjectDto;
import com.dgc.dm.core.generator.PojoGenerator;
import javassist.CannotCompileException;
import javassist.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

@Slf4j
@Controller
public class ProcessExcelController implements HandlerExceptionResolver {

    private static final int ROW_ONE = 1;
    private static final int ROW_ZERO = 0;
    private static final int SHEET_ZERO = 0;

    @Autowired
    DbServer dbServer;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response,
                                         Object object, Exception exc) {
        ModelAndView modelAndView = new ModelAndView("file");
        if (exc instanceof MaxUploadSizeExceededException) {
            modelAndView.getModel().put("message", "File size exceeds limit!");
        }
        return modelAndView;
    }

    @RequestMapping(value = "/uploadFile", method = RequestMethod.POST)
    public ModelAndView uploadFile(@RequestParam("projectName") String projectName, @RequestParam("file") MultipartFile file,
                                   ModelMap modelMap) {

        log.info("processing file " + file.getOriginalFilename() + " for " + projectName);

        ModelAndView modelAndView = new ModelAndView("decision");
        ProjectDto project = createProject(file, projectName);
        List<Object> excelObjs = processExcel(file, project);

        log.info("processed (" + excelObjs.size() + ") rows");

        modelAndView.getModel().put("message", "File uploaded successfully!");

        Map<String, List<Map<String, Object>>> filterList = getModelMapWithFilters();
//TODO to be optimized
        List<FilterDto> filterDtoList = new ArrayList<>();

        Iterator<Map<String, Object>> entryIterator = filterList.get("filterList").iterator();
        while (entryIterator.hasNext()) {
            Map<String, Object> filterIterator = entryIterator.next();

            String filterName = (String) filterIterator.get("name");
            if (filterName.equals("rowId")) {
                // Don't send rowId to decision view
                entryIterator.remove();
            } else {
                filterDtoList.add(FilterDto.builder().
                        id((Integer) filterIterator.get("ID")).
                        name(filterName).
                        filterClass((String) filterIterator.get("class")).
                        project(project).
                        build()
                );
            }
        }
//TODO END to be optimized
        modelAndView.addAllObjects(filterList);
        FilterCreationDto form = new FilterCreationDto(filterDtoList);
        modelAndView.addObject("form", form);

        return modelAndView;
    }

    private Map<String, List<Map<String, Object>>> getModelMapWithFilters() {
        List<Map<String, Object>> filters = dbServer.getFilters();

        Map<String, List<Map<String, Object>>> modelMap = new HashMap<>();
        modelMap.put("filterList", filters);

        return modelMap;
    }

    private ProjectDto createProject(final MultipartFile file, final String projectName) {
        log.info("Creating project " + projectName);
        Project project = dbServer.createProject(projectName);
        log.info("Creating project " + projectName);
        return modelMapper.map(project, ProjectDto.class);
    }

    private List<Object> processExcel(final MultipartFile file, final ProjectDto project) {

        List<Object> excelObjs = new ArrayList<>();
        try {
            final HSSFSheet worksheet = getWorkSheet(file);

            log.info("Getting Excel's column names");
            Map<String, Class<?>> colMapByName = getColumnNames(worksheet.getRow(ROW_ZERO), worksheet.getRow(ROW_ONE));

            log.info("Adding Excel's column(s) to Filters table");
            dbServer.createAndPopulateFilterTable(colMapByName, project);

            processExcelRows(worksheet, colMapByName, excelObjs, project);

        } catch (Exception e) {
            log.error("Error " + e.getMessage());
            e.printStackTrace();
        }

        return excelObjs;
    }

    private void processExcelRows(final HSSFSheet worksheet, final Map<String, Class<?>> columns, List<Object> excelObjs, final ProjectDto project) throws IOException, CannotCompileException, NotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {

        if (columns != null && columns.size() > 0) {
            log.info("Generating dynamic class");

            Class<? extends CommonDto> generatedObj = PojoGenerator.generate("com.dgc.dm.core.dto.Pojo$Generated", columns);
            log.info("Generated dynamic class: " + generatedObj.getName());

            log.info("Populating generated dynamic class with Excel's row values");

            List<Object[]> infoToBePersisted = new ArrayList<>();
            for (int rowNumber = 1; rowNumber < worksheet.getPhysicalNumberOfRows(); rowNumber++) {
                Object[] info = populateGeneratedObject(project, worksheet.getRow(rowNumber), generatedObj, columns, excelObjs, rowNumber);
                if (info != null) {
                    infoToBePersisted.add(info);
                }
            }
            dbServer.persistExcelRows(getInsertSentence(columns, project), infoToBePersisted);
        } else {
            log.error("No columns found on File");
        }

    }

    private String getInsertSentence(final Map<String, Class<?>> columns, final ProjectDto project) {
        StringBuilder insertQuery = new StringBuilder("insert into commonDatas ( rowId, ");

        for (Map.Entry<String, Class<?>> column : columns.entrySet()) {
            insertQuery.append(column.getKey()).append(",");
        }
        insertQuery = new StringBuilder(insertQuery.toString().replaceAll("[,]$", ", project) "));

        insertQuery.append(" values(").append(new String(new char[columns.size() + 1]).replace("\0", "?,"));

        insertQuery = new StringBuilder(insertQuery.toString().replaceAll("[,]$", "," + project.getId() + ")"));

        return insertQuery.toString();
    }

    private HSSFSheet getWorkSheet(final MultipartFile file) throws IOException {
        log.info("Getting workSheet from file " + file.getName());
        final HSSFWorkbook workbook = new HSSFWorkbook(file.getInputStream());
        return workbook.getSheetAt(SHEET_ZERO);
    }

    private Object[] populateGeneratedObject(final ProjectDto project, HSSFRow row, Class<? extends CommonDto> generatedObj, Map<String, Class<?>> columns,
                                             List<Object> excelObjs, int rowNumber) throws IllegalAccessException, IllegalArgumentException,
            SecurityException, InstantiationException, NoSuchMethodException, InvocationTargetException {

        log.trace("populating dynamic class with Excel row number " + rowNumber);

        Object[] insertQueryValues = new Object[]{};

        Iterator<Cell> excelRowIterator = row.cellIterator();
        while (excelRowIterator.hasNext()) {

            CommonDto obj = generatedObj.newInstance();
            for (Map.Entry<String, Class<?>> column : columns.entrySet()) {
                insertQueryValues = appendValueToObjectArray(insertQueryValues, populateDynamicClassProperty(generatedObj, column, excelRowIterator, obj));
            }
            if (isArrayEmpty(insertQueryValues)) {
                insertQueryValues = new Object[]{};
            } else {
                insertQueryValues = appendValueToObjectArray(insertQueryValues, rowNumber);
                obj.setRowId(rowNumber);
                obj.setProject(project);
                excelObjs.add(obj);

                log.trace("added object (" + obj + ") by row( " + rowNumber + ")");
            }
        }
        if (isArrayEmpty(insertQueryValues)) {
            return null;
        }
        return insertQueryValues;
    }

    private boolean isArrayEmpty(Object[] array) {

        for (Object ob : array) {
            if (ob != null) {
                return false;
            }
        }
        return true;
    }

    private Object[] appendValueToObjectArray(Object[] obj, Object newObj) {
        ArrayList<Object> temp = new ArrayList<>(Arrays.asList(obj));
        temp.add(newObj);
        return temp.toArray();
    }

    private String populateDynamicClassProperty(Class<? extends CommonDto> generatedObj, Map.Entry<String, Class<?>> column,
                                                Iterator<Cell> excelRowIterator, CommonDto obj)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        log.trace("Populating property " + column.getKey() + " with value " + column.getValue());
        String setMethod = "set" + StringUtils.capitalize(column.getKey());
        if (excelRowIterator.hasNext()) {
            String cellValue = populateMethodParameter(setMethod, generatedObj, column.getValue(), excelRowIterator.next(), obj);
            log.trace("Populated property " + column.getKey() + " with value " + cellValue);
            return cellValue;
        } else {
            log.trace("Populated property " + column.getKey() + " with null value ");
            return null;
        }
    }

    private String populateMethodParameter(String setMethod, Class<? extends CommonDto> generatedObj, Class<?> columnClass, Cell cell, CommonDto obj) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {

        Class<?> cellClass = getCellClass((HSSFCell) cell);
        if (cellClass == null) {
            log.trace("cell is BLANK");
            return null;
        } else {
            log.trace("Populating method: " + setMethod + ", class: " + cellClass.getName() + ", value: " + cell.toString());

            if (cellClass.isAssignableFrom(Date.class)) {
                generatedObj.getMethod(setMethod, columnClass).invoke(obj,
                        cell.getDateCellValue());
                return cell.getDateCellValue() + "";
            } else if (cellClass.isAssignableFrom(Double.class)) {
                generatedObj.getMethod(setMethod, columnClass).invoke(obj,
                        cell.getNumericCellValue());
                return cell.getNumericCellValue() + "";
            } else {
                generatedObj.getMethod(setMethod, columnClass).invoke(obj,
                        cell.getStringCellValue());
                return cell.getStringCellValue();
            }
        }
    }

    private LinkedHashMap<String, Class<?>> getColumnNames(final HSSFRow firstRow, final HSSFRow secondRow) {

        int colNum = firstRow.getPhysicalNumberOfCells();
        log.info("Found " + colNum + " column(s) on Excel");

        LinkedHashMap<String, Class<?>> colMapByName = new LinkedHashMap<>();
        if (firstRow.cellIterator().hasNext()) {
            for (int j = 0; j < colNum; j++) {
                HSSFCell cell = firstRow.getCell(j);
                if (cell != null) {
                    Class<?> cellClass = getCellClass(secondRow.getCell(j));
                    colMapByName.put(getColumnNameByCellValue(cell.getStringCellValue()), cellClass);
                    log.info("Processed column(" + j + "), columnName " + cell.getStringCellValue() + ", class " + Objects.requireNonNull(cellClass).getName());
                }
            }
        }
        return colMapByName;
    }

    private String getColumnNameByCellValue(String value) {
        StringBuilder sb = new StringBuilder();

        boolean lower = true;
        for (int charInd = 0; charInd < value.length(); ++charInd) {
            final char valueChar = value.charAt(charInd);
            if (valueChar == ' ' || valueChar == '_') {
                lower = false;
            } else if (lower) {
                sb.append(Character.toLowerCase(valueChar));
            } else {
                sb.append(Character.toUpperCase(valueChar));
                lower = true;
            }
        }

        return sb.toString().replaceAll("\\s+", "").replaceAll("_", "");
    }

    private Class<?> getCellClass(final HSSFCell cell) {

        switch (cell.getCellType()) {
            case STRING:
                if (isDateCell(cell)) {
                    return Date.class;
                }
                return String.class;
            case NUMERIC:
                if (isDateCell(cell)) {
                    return Date.class;
                }
                return Double.class;
            case BLANK:
                return null;
            default:
                return String.class;
        }

    }

    /**
     * @param cell
     * @return true if cellType is Date
     */
    private boolean isDateCell(HSSFCell cell) {
        try {
            return DateUtil.isCellDateFormatted(cell);
        } catch (java.lang.IllegalStateException e) {
            //Nothing to do
            return false;
        }
    }

}
