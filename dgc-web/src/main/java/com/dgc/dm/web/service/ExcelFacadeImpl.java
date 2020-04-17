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
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
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

    @Transactional
    @Override
    public ProjectDto processExcel(MultipartFile file, String projectName) {
        log.info("Processing Excel file");
        try {
            ProjectDto project = getProjectService().createProject(projectName);

            HSSFSheet worksheet = this.getWorkSheet(file);

            final Map<String, Class<?>> colMapByName = this.getExcelColumnNames(worksheet);

            this.getDataService().createDataTable(colMapByName, project);

            final List<Object> excelObjs = new ArrayList<>();
            this.processExcelRows(worksheet, colMapByName, excelObjs, project);
            getFilterService().persistFilterList(generateFilterList(colMapByName, project), project);

            log.info("processed (" + excelObjs.size() + ") rows");

            return project;
        } catch (final Exception e) {
            log.error("Error " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private List<FilterDto> generateFilterList(Map<String, Class<?>> columns, ProjectDto project) {
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

    private void processExcelRows(HSSFSheet worksheet, Map<String, Class<?>> columns, final List<Object> excelObjs, ProjectDto project) throws IOException, CannotCompileException, NotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        if (columns != null && columns.size() > 0) {
            log.info("Generating dynamic class");

            final Class<? extends CommonDto> generatedObj = PojoGenerator.generate("com.dgc.dm.core.dto.Pojo$Generated", columns);
            log.info("Generated dynamic class: " + generatedObj.getName());

            log.info("Populating generated dynamic class with Excel's row values");

            final List<Object[]> infoToBePersisted = new ArrayList<>();
            for (int rowNumber = 1; rowNumber < worksheet.getPhysicalNumberOfRows(); rowNumber++) {
                final Object[] info = this.populateGeneratedObject(project, worksheet.getRow(rowNumber), generatedObj, columns, excelObjs, rowNumber);
                if (info != null) {
                    infoToBePersisted.add(info);
                }
            }
            getDataService().persistData(this.getInsertSentence(columns, project.getCommonDataTableName()), infoToBePersisted);
        } else {
            log.error("No columns found on File");
        }
    }

    private String getInsertSentence(Map<String, Class<?>> columns, String commonDataTableName) {
        StringBuilder insertQuery = new StringBuilder("insert into " + commonDataTableName + " (");

        for (final Map.Entry<String, Class<?>> column : columns.entrySet()) {
            insertQuery.append(column.getKey()).append(",");
        }
        insertQuery = new StringBuilder(insertQuery.toString().replaceAll("[,]$", ", project, rowId) "));

        insertQuery.append(" values(").append(new String(new char[columns.size() + 2]).replace("\0", "?,"));

        insertQuery = new StringBuilder(insertQuery.toString().replaceAll("[,]$", ")"));

        return insertQuery.toString();
    }

    private Object[] populateGeneratedObject(ProjectDto project, final HSSFRow row, final Class<? extends CommonDto> generatedObj, final Map<String, Class<?>> columns,
                                             final List<Object> excelObjs, final int rowNumber) throws IllegalAccessException, IllegalArgumentException,
            SecurityException, InstantiationException, NoSuchMethodException, InvocationTargetException {

        log.trace("populating dynamic class with Excel row number " + rowNumber);

        Object[] insertQueryValues = {};

        final Iterator<Cell> excelRowIterator = row.cellIterator();
        while (excelRowIterator.hasNext()) {

            final CommonDto obj = generatedObj.newInstance();
            for (final Map.Entry<String, Class<?>> column : columns.entrySet()) {
                insertQueryValues = this.appendValueToObjectArray(insertQueryValues, this.populateDynamicClassProperty(generatedObj, column, excelRowIterator, obj));
            }
            //Add projectId
            insertQueryValues = this.appendValueToObjectArray(insertQueryValues, project.getId());
            if (this.isArrayEmpty(insertQueryValues)) {
                insertQueryValues = new Object[]{};
            } else {
                insertQueryValues = this.appendValueToObjectArray(insertQueryValues, rowNumber);
                obj.setRowId(rowNumber);
                obj.setProject(project);
                excelObjs.add(obj);

                log.trace("added object (" + obj + ") by row( " + rowNumber + ")");
            }
        }
        if (this.isArrayEmpty(insertQueryValues)) {
            return null;
        }
        return insertQueryValues;
    }

    private boolean isArrayEmpty(final Object[] array) {

        for (final Object ob : array) {
            if (ob != null) {
                return false;
            }
        }
        return true;
    }


    private Object[] appendValueToObjectArray(final Object[] obj, final Object newObj) {
        final ArrayList<Object> temp = new ArrayList<>(Arrays.asList(obj));
        temp.add(newObj);
        return temp.toArray();
    }

    private String populateDynamicClassProperty(final Class<? extends CommonDto> generatedObj, final Map.Entry<String, Class<?>> column,
                                                final Iterator<Cell> excelRowIterator, final CommonDto obj)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        log.trace("Populating property " + column.getKey() + " with value " + column.getValue());
        final String setMethod = "set" + StringUtils.capitalize(column.getKey());
        if (excelRowIterator.hasNext()) {
            final String cellValue = this.populateMethodParameter(setMethod, generatedObj, column.getValue(), excelRowIterator.next(), obj);
            log.trace("Populated property " + column.getKey() + " with value " + cellValue);
            return cellValue;
        } else {
            log.trace("Populated property " + column.getKey() + " with null value ");
            return null;
        }
    }

    private String populateMethodParameter(final String setMethod, final Class<? extends CommonDto> generatedObj, final Class<?> columnClass, final Cell cell, final CommonDto obj) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {

        final Class<?> cellClass = this.getCellClass((HSSFCell) cell);
        if (cellClass == null) {
            log.trace("cell is BLANK");
            return null;
        } else {
            log.trace("Populating method: " + setMethod + ", class: " + cellClass.getName() + ", value: " + cell);

            Class<?> colClass = columnClass.equals(Email.class) ? String.class : columnClass;

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

    private HSSFSheet getWorkSheet(MultipartFile file) throws IOException {
        log.info("Getting workSheet from file " + file.getName());
        HSSFWorkbook workbook = new HSSFWorkbook(file.getInputStream());
        return workbook.getSheetAt(SHEET_ZERO);
    }

    private LinkedHashMap<String, Class<?>> getExcelColumnNames(HSSFSheet worksheet) {
        log.info("Getting Excel's column names");
        final HSSFRow firstRow = worksheet.getRow(ROW_ZERO);
        final HSSFRow secondRow = worksheet.getRow(ROW_ONE);

        final int columnsSize = firstRow.getPhysicalNumberOfCells();
        log.info("Found " + columnsSize + " column(s) on Excel");

        final LinkedHashMap<String, Class<?>> colMapByName = new LinkedHashMap<>();
        if (firstRow.cellIterator().hasNext()) {
            for (int j = 0; j < columnsSize; j++) {
                final HSSFCell cell = firstRow.getCell(j);
                if (cell != null) {
                    final Class<?> cellClass = this.getCellClass(secondRow.getCell(j), worksheet, j, true);
                    colMapByName.put(this.getColumnNameByCellValue(cell.getStringCellValue()), cellClass);
                    log.trace("Processed column(" + j + "), columnName " + cell.getStringCellValue() + ", class " + ((cellClass == null) ? "NULL" : cellClass.getName()));
                }
            }
        }
        return colMapByName;
    }

    private String getColumnNameByCellValue(final String value) {
        final StringBuilder sb = new StringBuilder();

        boolean lower = true;
        for (int charInd = 0; charInd < value.length(); ++charInd) {
            char valueChar = value.charAt(charInd);
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

    private Class<?> getCellClass(HSSFCell cell) {
        return getCellClass(cell, null, 0, false);
    }

    private Class<?> getCellClass(HSSFCell cell, HSSFSheet worksheet, int cellNumber, boolean goOverWorkSheet) {
        final HSSFCell cellToBeProcessed = getNextCellNoNull(cell, worksheet, cellNumber, goOverWorkSheet);

        if (cellToBeProcessed == null) {
            log.warn("cell " + cell.getStringCellValue() + " is null in all sheet");
            return String.class;
        }

        switch (cellToBeProcessed.getCellType()) {
            case STRING:
                if (cellToBeProcessed.getStringCellValue().contains("@")) {
                    return Email.class;
                } else if (this.isDateCell(cellToBeProcessed)) {
                    return Date.class;
                }
                return String.class;
            case NUMERIC:
                if (this.isDateCell(cellToBeProcessed)) {
                    return Date.class;
                }
                return Double.class;
            case BLANK:
                return null;
            default:
                return String.class;
        }
    }

    private HSSFCell getNextCellNoNull(HSSFCell cell, HSSFSheet worksheet, int cellNumber, boolean goOverWorkSheet) {
        HSSFCell cellToBeProcessed = cell;
        if (cell == null) {
            if (goOverWorkSheet) {
                for (int i = 2; i < worksheet.getLastRowNum() && cellToBeProcessed == null; i++) {
                    final HSSFRow row = worksheet.getRow(i);
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
    private boolean isDateCell(final HSSFCell cell) {
        try {
            return DateUtil.isCellDateFormatted(cell);
        } catch (final java.lang.IllegalStateException e) {
            //Nothing to do
            return false;
        }
    }
}
