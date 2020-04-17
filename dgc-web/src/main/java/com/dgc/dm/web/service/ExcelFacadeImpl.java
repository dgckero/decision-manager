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
    public ProjectDto processExcel(final MultipartFile file, final String projectName) {
        log.info("Processing Excel file");
        try {
            final ProjectDto project = this.getProjectService().createProject(projectName);

            final HSSFSheet worksheet = getWorkSheet(file);

            Map<String, Class<?>> colMapByName = getExcelColumnNames(worksheet);

            getDataService().createDataTable(colMapByName, project);

            List<Object> excelObjs = new ArrayList<>();
            processExcelRows(worksheet, colMapByName, excelObjs, project);
            this.getFilterService().persistFilterList(this.generateFilterList(colMapByName, project), project);

            log.info("processed (" + excelObjs.size() + ") rows");

            return project;
        } catch (Exception e) {
            log.error("Error " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private List<FilterDto> generateFilterList(final Map<String, Class<?>> columns, final ProjectDto project) {
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
            this.getDataService().persistData(getInsertSentence(columns, project.getCommonDataTableName()), infoToBePersisted);
        } else {
            log.error("No columns found on File");
        }
    }

    private String getInsertSentence(final Map<String, Class<?>> columns, final String commonDataTableName) {
        StringBuilder insertQuery = new StringBuilder("insert into " + commonDataTableName + " (");

        for (Map.Entry<String, Class<?>> column : columns.entrySet()) {
            insertQuery.append(column.getKey()).append(",");
        }
        insertQuery = new StringBuilder(insertQuery.toString().replaceAll("[,]$", ", project, rowId) "));

        insertQuery.append(" values(").append(new String(new char[columns.size() + 2]).replace("\0", "?,"));

        insertQuery = new StringBuilder(insertQuery.toString().replaceAll("[,]$", ")"));

        return insertQuery.toString();
    }

    private Object[] populateGeneratedObject(final ProjectDto project, HSSFRow row, Class<? extends CommonDto> generatedObj, Map<String, Class<?>> columns,
                                             List<Object> excelObjs, int rowNumber) throws IllegalAccessException, IllegalArgumentException,
            SecurityException, InstantiationException, NoSuchMethodException, InvocationTargetException {

        log.trace("populating dynamic class with Excel row number " + rowNumber);

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
            log.trace("Populating method: " + setMethod + ", class: " + cellClass.getName() + ", value: " + cell);

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

    private HSSFSheet getWorkSheet(final MultipartFile file) throws IOException {
        log.info("Getting workSheet from file " + file.getName());
        final HSSFWorkbook workbook = new HSSFWorkbook(file.getInputStream());
        return workbook.getSheetAt(SHEET_ZERO);
    }

    private LinkedHashMap<String, Class<?>> getExcelColumnNames(final HSSFSheet worksheet) {
        log.info("Getting Excel's column names");
        HSSFRow firstRow = worksheet.getRow(ROW_ZERO);
        HSSFRow secondRow = worksheet.getRow(ROW_ONE);

        int columnsSize = firstRow.getPhysicalNumberOfCells();
        log.info("Found " + columnsSize + " column(s) on Excel");

        LinkedHashMap<String, Class<?>> colMapByName = new LinkedHashMap<>();
        if (firstRow.cellIterator().hasNext()) {
            for (int j = 0; j < columnsSize; j++) {
                HSSFCell cell = firstRow.getCell(j);
                if (cell != null) {
                    Class<?> cellClass = getCellClass(secondRow.getCell(j), worksheet, j, true);
                    colMapByName.put(getColumnNameByCellValue(cell.getStringCellValue()), cellClass);
                    log.trace("Processed column(" + j + "), columnName " + cell.getStringCellValue() + ", class " + ((cellClass == null) ? "NULL" : cellClass.getName()));
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
            log.warn("cell " + cell.getStringCellValue() + " is null in all sheet");
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
