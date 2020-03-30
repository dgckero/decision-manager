/*
  @author david
 */

package com.dgc.dm.web.controller;

import com.dgc.dm.core.db.model.Filter;
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
import javax.transaction.Transactional;
import javax.validation.constraints.Email;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Controller
public class ProcessExcelController implements HandlerExceptionResolver {

    private static final int ROW_ONE = 1;
    private static final int ROW_ZERO = 0;
    private static final int SHEET_ZERO = 0;
    private static final Integer CONTACT_FILTER = Integer.valueOf(1);

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

        ProcessExcelController.log.info("processing file " + file.getOriginalFilename() + " for " + projectName);

        ModelAndView modelAndView = new ModelAndView("decision");
        ProjectDto project = createProject(file, projectName);
        List<Object> excelObjs = processExcel(file, project);

        ProcessExcelController.log.info("processed (" + excelObjs.size() + ") rows");

        modelAndView.getModel().put("project", project);
        modelAndView.getModel().put("message", "File uploaded successfully!");

        Map<String, List<Map<String, Object>>> filterList = getModelMapWithFilters(project);
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
                        contactFilter(filterIterator.get("contactFilter").equals(ProcessExcelController.CONTACT_FILTER)).
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

    private Map<String, List<Map<String, Object>>> getModelMapWithFilters(ProjectDto project) {
        List<Map<String, Object>> filters = dbServer.getFilters(project);

        Map<String, List<Map<String, Object>>> modelMap = new HashMap<>();
        modelMap.put("filterList", filters);

        return modelMap;
    }

    private ProjectDto createProject(final MultipartFile file, final String projectName) {
        ProcessExcelController.log.info("Creating project " + projectName);
        Project project = dbServer.createProject(projectName);
        ProcessExcelController.log.info("Creating project " + projectName);
        return modelMapper.map(project, ProjectDto.class);
    }

    private List<Object> processExcel(final MultipartFile file, final ProjectDto project) {

        List<Object> excelObjs = new ArrayList<>();
        try {
            final HSSFSheet worksheet = getWorkSheet(file);

            ProcessExcelController.log.info("Getting Excel's column names");
            Map<String, Class<?>> colMapByName = getColumnNames(worksheet);

            List<Filter> filterList = dbServer.createCommonDatasTable(colMapByName, project);

            processExcelRows(worksheet, colMapByName, excelObjs, project);

            persistFilterList(filterList, project);

        } catch (Exception e) {
            ProcessExcelController.log.error("Error " + e.getMessage());
            e.printStackTrace();
        }

        return excelObjs;
    }

    /**
     * Create filter table and persist filters in this new table
     *
     * @param filterList list of filters
     * @param project
     */
    @Transactional
    private void persistFilterList(List<Filter> filterList, final ProjectDto project) {
        dbServer.createFilterTable(project);

        filterList = this.markContactFilter(filterList);

        dbServer.persistFilterList(filterList);
    }

    private List<Filter> markContactFilter(final List<Filter> filterList) {
        final AtomicInteger itemNumber = new AtomicInteger();

        filterList.stream()
                .filter(flt -> {
                    String filterClass = flt.getFilterClass();
                    itemNumber.getAndIncrement();
                    return filterClass.equals(Email.class.getSimpleName());
                })
                .forEach(
                        s ->
                                filterList.set(
                                        (itemNumber.get() - 1),
                                        Filter.builder()
                                                .name(s.getName())
                                                .project(s.getProject())

                                                .filterClass(String.class.getSimpleName())
                                                .value(s.getValue())
                                                .active(s.getActive())
                                                .contactFilter(Boolean.TRUE)
                                                .build()
                                )
                );

        return filterList;
    }

    private void processExcelRows(final HSSFSheet worksheet, final Map<String, Class<?>> columns, List<Object> excelObjs, final ProjectDto project) throws IOException, CannotCompileException, NotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        if (columns != null && columns.size() > 0) {
            ProcessExcelController.log.info("Generating dynamic class");

            Class<? extends CommonDto> generatedObj = PojoGenerator.generate("com.dgc.dm.core.dto.Pojo$Generated", columns);
            ProcessExcelController.log.info("Generated dynamic class: " + generatedObj.getName());

            ProcessExcelController.log.info("Populating generated dynamic class with Excel's row values");

            List<Object[]> infoToBePersisted = new ArrayList<>();
            for (int rowNumber = 1; rowNumber < worksheet.getPhysicalNumberOfRows(); rowNumber++) {
                Object[] info = populateGeneratedObject(project, worksheet.getRow(rowNumber), generatedObj, columns, excelObjs, rowNumber);
                if (info != null) {
                    infoToBePersisted.add(info);
                }
            }
            dbServer.persistExcelRows(getInsertSentence(columns), infoToBePersisted);
        } else {
            ProcessExcelController.log.error("No columns found on File");
        }
    }

    private String getInsertSentence(final Map<String, Class<?>> columns) {
        StringBuilder insertQuery = new StringBuilder("insert into commonDatas (");

        for (Map.Entry<String, Class<?>> column : columns.entrySet()) {
            insertQuery.append(column.getKey()).append(",");
        }
        insertQuery = new StringBuilder(insertQuery.toString().replaceAll("[,]$", ", project, rowId) "));

        insertQuery.append(" values(").append(new String(new char[columns.size() + 2]).replace("\0", "?,"));

        insertQuery = new StringBuilder(insertQuery.toString().replaceAll("[,]$", ")"));

        return insertQuery.toString();
    }

    private HSSFSheet getWorkSheet(final MultipartFile file) throws IOException {
        ProcessExcelController.log.info("Getting workSheet from file " + file.getName());
        final HSSFWorkbook workbook = new HSSFWorkbook(file.getInputStream());
        return workbook.getSheetAt(SHEET_ZERO);
    }

    private Object[] populateGeneratedObject(final ProjectDto project, HSSFRow row, Class<? extends CommonDto> generatedObj, Map<String, Class<?>> columns,
                                             List<Object> excelObjs, int rowNumber) throws IllegalAccessException, IllegalArgumentException,
            SecurityException, InstantiationException, NoSuchMethodException, InvocationTargetException {

        ProcessExcelController.log.trace("populating dynamic class with Excel row number " + rowNumber);

        Object[] insertQueryValues = {};

        Iterator<Cell> excelRowIterator = row.cellIterator();
        while (excelRowIterator.hasNext()) {

            CommonDto obj = generatedObj.newInstance();
            for (Map.Entry<String, Class<?>> column : columns.entrySet()) {
                insertQueryValues = appendValueToObjectArray(insertQueryValues, populateDynamicClassProperty(generatedObj, column, excelRowIterator, obj));
            }
            //Add projectId
            insertQueryValues = appendValueToObjectArray(insertQueryValues, project.getId());
            if (isArrayEmpty(insertQueryValues)) {
                insertQueryValues = new Object[]{};
            } else {
                insertQueryValues = appendValueToObjectArray(insertQueryValues, rowNumber);
                obj.setRowId(rowNumber);
                obj.setProject(project);
                excelObjs.add(obj);

                ProcessExcelController.log.trace("added object (" + obj + ") by row( " + rowNumber + ")");
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
        ProcessExcelController.log.trace("Populating property " + column.getKey() + " with value " + column.getValue());
        String setMethod = "set" + StringUtils.capitalize(column.getKey());
        if (excelRowIterator.hasNext()) {
            String cellValue = populateMethodParameter(setMethod, generatedObj, column.getValue(), excelRowIterator.next(), obj);
            ProcessExcelController.log.trace("Populated property " + column.getKey() + " with value " + cellValue);
            return cellValue;
        } else {
            ProcessExcelController.log.trace("Populated property " + column.getKey() + " with null value ");
            return null;
        }
    }

    private String populateMethodParameter(String setMethod, Class<? extends CommonDto> generatedObj, Class<?> columnClass, Cell cell, CommonDto obj) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {

        Class<?> cellClass = getCellClass((HSSFCell) cell);
        if (cellClass == null) {
            ProcessExcelController.log.trace("cell is BLANK");
            return null;
        } else {
            ProcessExcelController.log.trace("Populating method: " + setMethod + ", class: " + cellClass.getName() + ", value: " + cell);

            final Class<?> colClass = columnClass.equals(Email.class) ? String.class : columnClass;

            if (cellClass.isAssignableFrom(Date.class)) {
                generatedObj.getMethod(setMethod, colClass).invoke(obj,
                        cell.getDateCellValue());
                return cell.getDateCellValue() + "";
            } else if (cellClass.isAssignableFrom(Double.class)) {
                generatedObj.getMethod(setMethod, colClass).invoke(obj,
                        cell.getNumericCellValue());
                return cell.getNumericCellValue() + "";
            } else {
                generatedObj.getMethod(setMethod, colClass).invoke(obj,
                        cell.getStringCellValue());
                return cell.getStringCellValue();
            }
        }
    }

    private LinkedHashMap<String, Class<?>> getColumnNames(final HSSFSheet worksheet) {

        HSSFRow firstRow = worksheet.getRow(ROW_ZERO);
        HSSFRow secondRow = worksheet.getRow(ROW_ONE);

        int columnsSize = firstRow.getPhysicalNumberOfCells();
        ProcessExcelController.log.info("Found " + columnsSize + " column(s) on Excel");

        LinkedHashMap<String, Class<?>> colMapByName = new LinkedHashMap<>();
        if (firstRow.cellIterator().hasNext()) {
            for (int j = 0; j < columnsSize; j++) {
                HSSFCell cell = firstRow.getCell(j);
                if (cell != null) {
                    Class<?> cellClass = getCellClass(secondRow.getCell(j), worksheet, j, true);
                    colMapByName.put(getColumnNameByCellValue(cell.getStringCellValue()), cellClass);
                    ProcessExcelController.log.info("Processed column(" + j + "), columnName " + cell.getStringCellValue() + ", class " + ((cellClass == null) ? "NULL" : cellClass.getName()));
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
        return this.getCellClass(cell, null, 0, false);
    }

    private Class<?> getCellClass(final HSSFCell cell, final HSSFSheet worksheet, final int cellNumber, final boolean goOverWorkSheet) {
        HSSFCell cellToBeProcessed = this.getNextCellNoNull(cell, worksheet, cellNumber, goOverWorkSheet);

        if (cellToBeProcessed == null) {
            ProcessExcelController.log.warn("cell " + cell.getStringCellValue() + " is null in all sheet");
            return String.class;
        }

        switch (cellToBeProcessed.getCellType()) {
            case STRING:
                if (cellToBeProcessed.getStringCellValue().contains("@")) {
                    return Email.class;
                } else if (isDateCell(cellToBeProcessed)) {
                    return Date.class;
                }
                return String.class;
            case NUMERIC:
                if (isDateCell(cellToBeProcessed)) {
                    return Date.class;
                }
                return Double.class;
            case BLANK:
                return null;
            default:
                return String.class;
        }
    }

    private HSSFCell getNextCellNoNull(final HSSFCell cell, final HSSFSheet worksheet, final int cellNumber, final boolean goOverWorkSheet) {
        HSSFCell cellToBeProcessed = cell;
        if (cell == null) {
            if (goOverWorkSheet) {
                for (int i = 2; i < worksheet.getLastRowNum() && cellToBeProcessed == null; i++) {
                    HSSFRow row = worksheet.getRow(i);
                    cellToBeProcessed = row.getCell(cellNumber);
                }
            } else {
                return null;
            }
        }
        return cellToBeProcessed;
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
