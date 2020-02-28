/**
 * @author david
 */

package com.dgc.dm.web.controller;

import com.dgc.dm.core.db.service.DbServer;
import com.dgc.dm.core.dto.CommonDto;
import com.dgc.dm.core.generator.PojoGenerator;
import javassist.CannotCompileException;
import javassist.NotFoundException;
import lombok.extern.log4j.Log4j2;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

@Log4j2
@Controller
public class ProcessExcelController implements HandlerExceptionResolver {

    @Autowired
    DbServer dbServer;

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
    public ModelAndView uploadFile(MultipartFile file) throws IOException {

        log.info("processing file ");

        ModelAndView modelAndView = new ModelAndView("decision");

        List<Object> excelObjs = processExcel(file);

        log.info("processed (" + excelObjs.size() + ") rows");

        modelAndView.getModel().put("message", "File uploaded successfully!");
        return modelAndView;
    }

    private List<Object> processExcel(MultipartFile file) {

        List<Object> excelObjs = new ArrayList<>();
        try {
            final HSSFSheet worksheet = getWorkSheet(file);

            processExcelRows(worksheet, getColumnNames(worksheet.getRow(0), worksheet.getRow(1)), excelObjs);

        } catch (Exception e) {
            log.error("Error " + e.getMessage());
            e.printStackTrace();
        }

        return excelObjs;
    }

    private void processExcelRows(final HSSFSheet worksheet, final Map<String, Class<?>> columns, List<Object> excelObjs) throws IOException, CannotCompileException, NotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {


        if (columns != null && columns.size() > 0) {
            log.info("Generating dynamic class");

            Class<? extends CommonDto> generatedObj = PojoGenerator.generate("com.dgc.dm.core.dto.Pojo$Generated", columns);
            log.info("Generated dynamic class: " + generatedObj.getName());

            log.info("Populating generated dynamic class with Excel's row values");
            for (int i = 1; i < worksheet.getPhysicalNumberOfRows(); i++) {
                populateGeneratedObject(worksheet.getRow(i), generatedObj, columns, excelObjs, i);
            }
        } else {
            log.error("No columns found on File");
        }


    }

    private HSSFSheet getWorkSheet(final MultipartFile file) throws IOException {
        log.info("Getting workSheet from file " + file.getName());
        final HSSFWorkbook workbook = new HSSFWorkbook(file.getInputStream());
        return workbook.getSheetAt(0);
    }

    private void populateGeneratedObject(HSSFRow row, Class<? extends CommonDto> generatedObj, Map<String, Class<?>> columns,
                                         List<Object> excelObjs, int rowNumber) throws IllegalAccessException, IllegalArgumentException,
            SecurityException, InstantiationException, NoSuchMethodException, InvocationTargetException {

        log.info("populating dynamic class with Excel row number " + rowNumber);

        Iterator<Cell> excelRowIterator = row.cellIterator();
        List<String> infoToBePersisted = new ArrayList<>();

        while (excelRowIterator.hasNext()) {
            String insertQuery = "insert into commonDatas ( id, ";
            String insertQueryValues = " values ( " + rowNumber + ", ";

            CommonDto obj = generatedObj.newInstance();
            for (Map.Entry<String, Class<?>> column : columns.entrySet()) {
                insertQuery += column.getKey() + ",";
                populateDynamicClassProperty(generatedObj, column, excelRowIterator, obj, insertQueryValues);
            }
            obj.setRowId(rowNumber);
            excelObjs.add(obj);

            insertQuery = insertQuery.replaceAll("[,]$", ") ");
            insertQueryValues = insertQueryValues.replaceAll("[,]$", ") ");

            infoToBePersisted.add(insertQuery + insertQueryValues);

            log.trace("added object (" + obj + ") by row( " + rowNumber + ")");
        }

        dbServer.persistExcelRows(infoToBePersisted);

    }

    private void populateDynamicClassProperty(Class<? extends CommonDto> generatedObj, Map.Entry<String, Class<?>> column,
                                              Iterator<Cell> excelRowIterator, CommonDto obj, String insertQueryValues)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        log.trace("Populating property " + column.getKey() + " with value " + column.getValue());

        String setMethod = "set" + StringUtils.capitalize(column.getKey());
        if (excelRowIterator.hasNext()) {
            populateMethodParameter(setMethod, generatedObj, column.getValue(), excelRowIterator.next(), insertQueryValues, obj);
        } // else set null to class's parameter

        log.trace("Populated property " + column.getKey() + " with value " + column.getValue());
    }

    private void populateMethodParameter(String setMethod, Class<? extends CommonDto> generatedObj, Class<?> columnClass, Cell cell, String insertQueryValues, CommonDto obj) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {

        Class<?> cellClass = getCellClass((HSSFCell) cell);

        log.trace("Populating method: " + setMethod + ", class: " + cellClass.getName() + ", value: " + cell.toString());

        if (cellClass.isAssignableFrom(Date.class)) {
            generatedObj.getMethod(setMethod, columnClass).invoke(obj,
                    cell.getDateCellValue());
            insertQueryValues += cell.getDateCellValue() + ",";
        } else if (cellClass.isAssignableFrom(Double.class)) {
            generatedObj.getMethod(setMethod, columnClass).invoke(obj,
                    cell.getNumericCellValue());
            insertQueryValues += cell.getNumericCellValue() + ",";
        } else {
            generatedObj.getMethod(setMethod, columnClass).invoke(obj,
                    cell.getStringCellValue());
            insertQueryValues += cell.getStringCellValue() + ",";
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
                    log.info("Processed column(" + j + "), columnName " + cell.getStringCellValue() + ", class " + cellClass.getName());
                }
            }
        }

        log.info("Adding Excel's column(s) to Filters table");
        dbServer.createAndPopulateFilterTable(colMapByName);

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
