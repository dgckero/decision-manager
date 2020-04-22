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
import java.util.*;

@Slf4j
@Service
public class ExcelFacadeImpl extends CommonFacade implements ExcelFacade {
    private static final int ROW_ONE = 1;
    private static final int ROW_ZERO = 0;
    private static final int SHEET_ZERO = 0;


    private static List<FilterDto> generateFilterList(final Map<String, Class<?>> columns, final ProjectDto project) {
        final List<FilterDto> filterList = new ArrayList<>();

        for (final Map.Entry<String, Class<?>> column : columns.entrySet()) {
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

    private static String getInsertSentence(final Map<String, Class<?>> columns, final String commonDataTableName) {
        StringBuilder insertQuery = new StringBuilder("insert into " + commonDataTableName + " (");

        for (final Map.Entry<String, Class<?>> column : columns.entrySet()) {
            insertQuery.append(column.getKey()).append(",");
        }
        insertQuery = new StringBuilder(insertQuery.toString().replaceAll("[,]$", ", project, rowId) "));

        insertQuery.append(" values(").append(new String(new char[columns.size() + 2]).replace("\0", "?,"));

        insertQuery = new StringBuilder(insertQuery.toString().replaceAll("[,]$", ")"));

        return insertQuery.toString();
    }

    private static boolean isArrayEmpty(final Object[] array) {
        for (final Object ob : array) {
            if (null != ob) {
                return false;
            }
        }
        return true;
    }

    private static Object[] appendValueToObjectArray(final Object[] obj, final Object newObj) {
        final ArrayList<Object> temp = new ArrayList<>(Arrays.asList(obj));
        temp.add(newObj);
        return temp.toArray();
    }

    private static Sheet getWorkSheet(final MultipartFile file) throws IOException {
        log.info("Getting workSheet from file {}", file.getName());
        final Workbook workbook = WorkbookFactory.create(file.getInputStream());
        return workbook.getSheetAt(SHEET_ZERO);
    }

    private static String getColumnNameByCellValue(final String value) {
        final StringBuilder sb = new StringBuilder();

        boolean lower = true;
        for (int charInd = 0; charInd < value.length(); ++charInd) {
            final char valueChar = value.charAt(charInd);
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

    private static Cell getNextCellNoNull(final Cell cell, final Sheet worksheet, final int cellNumber, final boolean goOverWorkSheet) {
        Cell cellToBeProcessed = cell;
        if (null == cell) {
            if (goOverWorkSheet) {
                for (int i = 2; i < worksheet.getLastRowNum() && null == cellToBeProcessed; i++) {
                    final Row row = worksheet.getRow(i);
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
    private static boolean isDateCell(final Cell cell) {
        try {
            return DateUtil.isCellDateFormatted(cell);
        } catch (final java.lang.IllegalStateException e) {
            //Nothing to do
            return false;
        }
    }

    private Map<String, Class<?>> compareExcelColumnNames(final Sheet worksheet, final MultipartFile file, final ProjectDto project) throws Exception {
        final Map<String, Class<?>> colMapByName = this.getExcelColumnNames(worksheet);
        if (colMapByName.isEmpty()) {
            log.warn("No columns found on file {}", file.getOriginalFilename());
            throw new Exception("No columns found on file " + file.getOriginalFilename());
        }
        log.debug("Found {} filters on excel {}", colMapByName.size(), file.getOriginalFilename());
        final List<Map<String, Object>> projectFilters = this.getFilterService().getFilters(project);
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
    public final void processExcel(final MultipartFile file, final ProjectDto project) throws Exception {
        log.info("Processing Excel file " + file.getOriginalFilename());

        final Sheet worksheet = getWorkSheet(file);
        final Map<String, Class<?>> colMapByName = this.compareExcelColumnNames(worksheet, file, project);
        this.processExcelRows(worksheet, colMapByName, project, this.getDataService().getCommonDataSize(project) + 1);
    }

    @Transactional
    @Override
    public final ProjectDto processExcel(final MultipartFile file, final String projectName) {
        log.info("Processing Excel file");
        try {
            final Sheet worksheet = getWorkSheet(file);
            final Map<String, Class<?>> colMapByName = this.getExcelColumnNames(worksheet);

            final ProjectDto project = this.createProjectModel(projectName, colMapByName);

            this.processExcelRows(worksheet, colMapByName, project, 0);
            this.getFilterService().persistFilterList(generateFilterList(colMapByName, project), project);

            return project;
        } catch (Exception e) {
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
    private ProjectDto createProjectModel(final String projectName, final Map<String, Class<?>> colMapByName) {
        final ProjectDto project = this.getProjectService().createProject(projectName);
        this.getDataService().createDataTable(colMapByName, project);
        return project;
    }

    private void processExcelRows(final Sheet worksheet, final Map<String, Class<?>> columns, final ProjectDto project, int rowIdNumber) throws IOException, CannotCompileException, NotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        if (null != columns && !columns.isEmpty()) {
            final List<Object> excelObjs = new ArrayList<>();

            log.info("Generating dynamic class");
            final Class<? extends CommonDto> generatedObj = PojoGenerator.generate("com.dgc.dm.core.dto.Pojo$Generated", columns);
            log.info("Generated dynamic class: {}", generatedObj.getName());

            log.info("Populating generated dynamic class with Excel's row values");
            final List<Object[]> infoToBePersisted = new ArrayList<>();
            for (int rowNumber = 1; rowNumber < worksheet.getPhysicalNumberOfRows(); rowNumber++, rowIdNumber++) {
                final Object[] info = this.populateGeneratedObject(project, worksheet.getRow(rowNumber), generatedObj, columns, excelObjs, rowIdNumber);
                if (null != info) {
                    infoToBePersisted.add(info);
                }
            }
            this.getDataService().persistData(getInsertSentence(columns, project.getCommonDataTableName()), infoToBePersisted);

            log.info("processed ({}) rows", excelObjs.size());
        } else {
            log.error("No columns found on File");
        }
    }

    private Object[] populateGeneratedObject(final ProjectDto project, final Row row, final Class<? extends CommonDto> generatedObj, final Map<String, Class<?>> columns,
                                             final List<Object> excelObjs, final int rowNumber) throws IllegalAccessException,
            InstantiationException, NoSuchMethodException, InvocationTargetException {

        log.trace("populating dynamic class with Excel row number {}", rowNumber);

        Object[] insertQueryValues = {};

        final Iterator<Cell> excelRowIterator = row.cellIterator();
        while (excelRowIterator.hasNext()) {

            final CommonDto obj = generatedObj.getConstructor().newInstance();
            for (final Map.Entry<String, Class<?>> column : columns.entrySet()) {
                insertQueryValues = appendValueToObjectArray(insertQueryValues, this.populateDynamicClassProperty(generatedObj, column, excelRowIterator, obj));
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

    private String populateDynamicClassProperty(final Class<? extends CommonDto> generatedObj, final Map.Entry<String, Class<?>> column,
                                                final Iterator<Cell> excelRowIterator, final CommonDto obj)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        log.trace("Populating property {} with value {}", column.getKey(), column.getValue());
        final String setMethod = "set" + StringUtils.capitalize(column.getKey());
        if (excelRowIterator.hasNext()) {
            final String cellValue = this.populateMethodParameter(setMethod, generatedObj, column.getValue(), excelRowIterator.next(), obj);
            log.trace("Populated property {} with value {}", column.getKey(), cellValue);
            return cellValue;
        } else {
            log.trace("Populated property {} with null value ", column.getKey());
            return null;
        }
    }

    private String populateMethodParameter(final String setMethod, final Class<? extends CommonDto> generatedObj, final Class<?> columnClass, final Cell cell, final CommonDto obj) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {

        final Class<?> cellClass = this.getCellClass(cell);
        if (null == cellClass) {
            log.trace("cell is BLANK");
            return null;
        } else {
            log.trace("Populating method: {}, class: {}, value: {}", setMethod, cellClass.getName(), cell);

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

    private LinkedHashMap<String, Class<?>> getExcelColumnNames(final Sheet worksheet) {

        log.info("Getting Excel's column names");
        final Row firstRow = worksheet.getRow(ROW_ZERO);
        final Row secondRow = worksheet.getRow(ROW_ONE);

        final int columnsSize = firstRow.getPhysicalNumberOfCells();
        log.info("Found {} column(s) on Excel", columnsSize);

        final LinkedHashMap<String, Class<?>> colMapByName = new LinkedHashMap<>();
        if (firstRow.cellIterator().hasNext()) {
            for (int j = 0; j < columnsSize; j++) {
                final Cell cell = firstRow.getCell(j);
                if (null != cell) {
                    final Class<?> cellClass = this.getCellClass(secondRow.getCell(j), worksheet, j, true);
                    colMapByName.put(getColumnNameByCellValue(cell.getStringCellValue()), cellClass);
                    log.trace("Processed column({}), columnName {}, class {}", j, cell.getStringCellValue(), (null == cellClass) ? "NULL" : cellClass.getName());
                }
            }
        }
        return colMapByName;
    }

    private Class<?> getCellClass(final Cell cell) {
        return this.getCellClass(cell, null, 0, false);
    }

    private Class<?> getCellClass(final Cell cell, final Sheet worksheet, final int cellNumber, final boolean goOverWorkSheet) {
        final Cell cellToBeProcessed = getNextCellNoNull(cell, worksheet, cellNumber, goOverWorkSheet);

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
