/*
  @author david
 */

package com.dgc.dm.web.service;

import com.dgc.dm.core.dto.CommonDto;
import com.dgc.dm.core.dto.FilterDto;
import com.dgc.dm.core.dto.ProjectDto;
import com.dgc.dm.core.generator.PojoGenerator;
import javassist.CannotCompileException;
import javassist.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import javax.validation.constraints.Email;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.*;

@Slf4j
@Service
public class ExcelFacadeImpl extends CommonFacade implements ExcelFacade {
    private static final int ROW_ONE = 1;
    private static final int ROW_ZERO = 0;
    private static final int SHEET_ZERO = 0;
    private final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

    private static List<FilterDto> generateFilterList(Map<String, Class<?>> columns, ProjectDto project) {
        List<FilterDto> filterList = new ArrayList<>();

        for (Map.Entry<String, Class<?>> column : columns.entrySet()) {
            filterList.add(FilterDto.builder().
                    name(column.getKey()).
                    filterClass(column.getValue().getSimpleName()).
                    active(Boolean.FALSE).
                    contactFilter(Boolean.FALSE).
                    project(project).
                    build());
        }

        return filterList;
    }

    private static String getInsertSentence(Map<String, Class<?>> columns, String commonDataTableName) {
        StringBuilder insertQuery = new StringBuilder("insert into " + commonDataTableName + " (");

        for (Map.Entry<String, Class<?>> column : columns.entrySet()) {
            insertQuery.append(column.getKey()).append(",");
        }
        insertQuery = new StringBuilder(insertQuery.toString().replaceAll("[,]$", ", project, rowId) "));

        insertQuery.append(" values(").append(new String(new char[columns.size() + 2]).replace("\0", "?,"));

        insertQuery = new StringBuilder(insertQuery.toString().replaceAll("[,]$", ")"));

        return insertQuery.toString();
    }

    private static boolean isArrayEmpty(Object[] array) {
        for (Object ob : array) {
            if (null != ob) {
                return false;
            }
        }
        return true;
    }

    private static Object[] appendValueToObjectArray(Object[] obj, Object newObj) {
        ArrayList<Object> temp = new ArrayList<>(Arrays.asList(obj));
        temp.add(newObj);
        return temp.toArray();
    }

    private static Sheet getWorkSheet(MultipartFile file) throws IOException {
        log.info("Getting workSheet from file {}", file.getName());
        Workbook workbook = WorkbookFactory.create(file.getInputStream());
        return workbook.getSheetAt(SHEET_ZERO);
    }

    private static String getColumnNameByCellValue(String value) {
        StringBuilder sb = new StringBuilder();

        boolean lower = true;
        for (int charInd = 0; charInd < value.length(); ++charInd) {
            char valueChar = value.charAt(charInd);
            if (' ' == valueChar || '_' == valueChar) {
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

    private static Cell getNextCellNoNull(Cell cell, Sheet worksheet, int cellNumber, boolean goOverWorkSheet) {
        Cell cellToBeProcessed = cell;
        if (null == cell) {
            if (goOverWorkSheet) {
                for (int i = 2; i < worksheet.getLastRowNum() && null == cellToBeProcessed; i++) {
                    Row row = worksheet.getRow(i);
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
    private static boolean isDateCell(Cell cell) {
        try {
            return DateUtil.isCellDateFormatted(cell);
        } catch (java.lang.IllegalStateException e) {
            //Nothing to do
            return false;
        }
    }

    private Map<String, Class<?>> compareExcelColumnNames(Sheet worksheet, MultipartFile file, ProjectDto project) throws Exception {
        Map<String, Class<?>> colMapByName = getExcelColumnNames(worksheet);
        if (colMapByName.isEmpty()) {
            log.warn("No columns found on file {}", file.getOriginalFilename());
            throw new Exception("No columns found on file " + file.getOriginalFilename());
        }
        log.debug("Found {} filters on excel {}", colMapByName.size(), file.getOriginalFilename());
        List<Map<String, Object>> projectFilters = getFilterService().getFilters(project);
        if (null == projectFilters || projectFilters.isEmpty()) {
            log.warn("No filters found for project {}", project);
            throw new Exception("No filters found for project " + project);
        } else if ((projectFilters.size() - 1) == colMapByName.size()) { // subtract 1 to project's filters because of the rowId column
            log.info("Excel file has same number of columns ({}) than filters ({}) for project {}", colMapByName.size(), projectFilters.size() - 1, project);
            return colMapByName;
        } else {
            log.warn("Excel file has NOT same number of columns ({}) than filters ({}) for project {}", colMapByName.size(), projectFilters.size(), project);
            throw new Exception("Excel file has NOT same number of columns (" + colMapByName.size() + ") than filters (" + projectFilters.size() + ") for project " + project.getName());
        }

    }

    @Transactional
    @Override
    public final void processExcel(MultipartFile file, ProjectDto project) throws Exception {
        log.info("Processing Excel file " + file.getOriginalFilename());

        Sheet worksheet = getWorkSheet(file);
        Map<String, Class<?>> colMapByName = compareExcelColumnNames(worksheet, file, project);
        processExcelRows(worksheet, colMapByName, project, getDataService().getCommonDataSize(project) + 1);
    }

    @Transactional
    @Override
    public final ProjectDto processExcel(MultipartFile file, String projectName) {
        log.info("Processing Excel file");
        try {
            Sheet worksheet = getWorkSheet(file);
            Map<String, Class<?>> colMapByName = getExcelColumnNames(worksheet);
            ProjectDto project = createProjectModel(projectName, colMapByName);
            processExcelRows(worksheet, colMapByName, project, 0);

            return project;
        } catch (final Exception e) {
            log.error("Error {}", e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Create Project and Data tables
     * Add Project information on Project table
     *
     * @param projectName
     * @param colMapByName
     * @return new Project
     */
    private ProjectDto createProjectModel(String projectName, Map<String, Class<?>> colMapByName) {
        ProjectDto project = getProjectService().createProject(projectName);
        getDataService().createDataTable(colMapByName, project);
        getFilterService().persistFilterList(generateFilterList(colMapByName, project), project);
        return project;
    }

    private void processExcelRows(Sheet worksheet, Map<String, Class<?>> columns, ProjectDto project, int rowIdNumber) throws IOException, CannotCompileException, NotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        if (null != columns && !columns.isEmpty()) {
            List<Object> excelObjs = new ArrayList<>();

            log.info("Generating dynamic class");
            Class<? extends CommonDto> generatedObj = PojoGenerator.generate("com.dgc.dm.core.dto.Pojo$Generated", columns);
            log.info("Generated dynamic class: {}", generatedObj.getName());

            log.info("Populating generated dynamic class with Excel's row values");
            List<Object[]> infoToBePersisted = new ArrayList<>();
            for (int rowNumber = 1; rowNumber < worksheet.getPhysicalNumberOfRows(); rowNumber++, rowIdNumber++) {
                Object[] info = populateGeneratedObject(project, worksheet.getRow(rowNumber), generatedObj, columns, excelObjs, rowIdNumber);
                if (null != info) {
                    infoToBePersisted.add(info);
                }
            }
            getDataService().persistData(getInsertSentence(columns, project.getCommonDataTableName()), infoToBePersisted);

            log.info("processed ({}) rows", excelObjs.size());
        } else {
            log.error("No columns found on File");
        }
    }

    private Object[] populateGeneratedObject(ProjectDto project, Row row, Class<? extends CommonDto> generatedObj, Map<String, Class<?>> columns,
                                             List<Object> excelObjs, int rowNumber) throws IllegalAccessException,
            InstantiationException, NoSuchMethodException, InvocationTargetException {

        log.trace("populating dynamic class with Excel row number {}", rowNumber);

        Object[] insertQueryValues = {};

        Iterator<Cell> excelRowIterator = row.cellIterator();
        while (excelRowIterator.hasNext()) {

            CommonDto obj = generatedObj.getConstructor().newInstance();
            for (Map.Entry<String, Class<?>> column : columns.entrySet()) {
                if (column.getValue() == Date.class) {
                    column.setValue(String.class);
                }
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

                log.trace("added object ({}) by row( {})", obj, rowNumber);
            }
        }
        if (isArrayEmpty(insertQueryValues)) {
            return null;
        }
        return insertQueryValues;
    }

    private String populateDynamicClassProperty(Class<? extends CommonDto> generatedObj, Map.Entry<String, Class<?>> column,
                                                Iterator<Cell> excelRowIterator, CommonDto obj)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        log.trace("Populating property {} with value {}", column.getKey(), column.getValue());
        String setMethod = "set" + StringUtils.capitalize(column.getKey());
        if (excelRowIterator.hasNext()) {
            String cellValue = populateMethodParameter(setMethod, generatedObj, column.getValue(), excelRowIterator.next(), obj);
            log.trace("Populated property {} with value {}", column.getKey(), cellValue);
            return cellValue;
        } else {
            log.trace("Populated property {} with null value ", column.getKey());
            return null;
        }
    }

    private String populateMethodParameter(String setMethod, Class<? extends CommonDto> generatedObj, Class<?> columnClass, Cell cell, CommonDto obj) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        log.trace("populateMethodParameter setMethod {}, columnClass {} ", setMethod, columnClass);
        Class<?> cellClass = getCellClass(cell);
        if (null == cellClass) {
            log.trace("cell is BLANK");
            return null;
        } else {
            log.trace("Populating method: {}, class: {}, value: {}", setMethod, cellClass.getName(), cell);
            Class<?> colClass = (columnClass.equals(Email.class)) ? String.class : columnClass;
            if (cellClass.isAssignableFrom(Date.class)) {
                String cellValue = format.format(cell.getDateCellValue());
                generatedObj.getMethod(setMethod, colClass).invoke(obj, cellValue);
                return cellValue;
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

    private LinkedHashMap<String, Class<?>> getExcelColumnNames(Sheet worksheet) {

        log.info("Getting Excel's column names");
        Row firstRow = worksheet.getRow(ROW_ZERO);
        Row secondRow = worksheet.getRow(ROW_ONE);

        int columnsSize = firstRow.getPhysicalNumberOfCells();
        log.info("Found {} column(s) on Excel", columnsSize);

        LinkedHashMap<String, Class<?>> colMapByName = new LinkedHashMap<>();
        if (firstRow.cellIterator().hasNext()) {
            for (int j = 0; j < columnsSize; j++) {
                Cell cell = firstRow.getCell(j);
                if (null != cell) {
                    Class<?> cellClass = getCellClass(secondRow.getCell(j), worksheet, j, true);
                    colMapByName.put(getColumnNameByCellValue(cell.getStringCellValue()), cellClass);
                    log.trace("Processed column({}), columnName {}, class {}", j, cell.getStringCellValue(), (null == cellClass) ? "NULL" : cellClass.getName());
                }
            }
        }
        return colMapByName;
    }

    private Class<?> getCellClass(Cell cell) {
        return getCellClass(cell, null, 0, false);
    }

    private Class<?> getCellClass(Cell cell, Sheet worksheet, int cellNumber, boolean goOverWorkSheet) {
        Cell cellToBeProcessed = getNextCellNoNull(cell, worksheet, cellNumber, goOverWorkSheet);

        if (null == cellToBeProcessed) {
            log.warn("Column number {} is null in all sheet", cellNumber);
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
}
