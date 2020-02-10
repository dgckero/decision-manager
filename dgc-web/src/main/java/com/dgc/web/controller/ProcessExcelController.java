/**
 *
 */
package com.dgc.web.controller;

import com.dgc.jbpm.core.dto.CommonDto;
import com.dgc.jbpm.core.util.PojoGenerator;
import lombok.extern.log4j.Log4j2;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.springframework.stereotype.Controller;
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
import java.util.regex.Pattern;

/**
 * @author david
 *
 */
@Controller
@Log4j2
public class ProcessExcelController implements HandlerExceptionResolver {

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

        log.info("processed (" + ((excelObjs == null) ? 0 : excelObjs.size()) + ") rows");

        modelAndView.getModel().put("message", "File uploaded successfully!");
        return modelAndView;
    }

    private List<Object> processExcel(MultipartFile file) throws IOException {

        List<Object> excelObjs = new ArrayList<>();
        try {
            final HSSFWorkbook workbook = new HSSFWorkbook(file.getInputStream());
            final HSSFSheet worksheet = workbook.getSheetAt(0);
            final HSSFRow firstRow = worksheet.getRow(0);
            SortedMap<String, Class<?>> props = getColumnNames(firstRow, worksheet.getRow(1));

            Class<? extends CommonDto> generatedObj = PojoGenerator.generate("com.dgc.jbpm.core.dto.Pojo$Generated",
                    props);

            for (int i = 1; i < worksheet.getPhysicalNumberOfRows(); i++) {
                fillGeneratedObject(worksheet.getRow(i), generatedObj, props, excelObjs);
            }

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return excelObjs;
    }

    private void fillGeneratedObject(HSSFRow row, Class<? extends CommonDto> generatedObj, Map<String, Class<?>> props,
                                     List<Object> excelObjs) throws IllegalAccessException, IllegalArgumentException,
            InvocationTargetException, NoSuchMethodException, SecurityException, InstantiationException {

        Iterator<Cell> cellIterator = row.cellIterator();

        if (props != null && props.size() > 0) {

            String firstPropKey = props.keySet().iterator().next();
            log.debug("Id column on Excel is: " + firstPropKey + ", class: " + props.get(firstPropKey));
            int rowNumber = 0;

            while (cellIterator.hasNext()) {
                CommonDto obj = generatedObj.newInstance();

                for (Map.Entry<String, Class<?>> prop : props.entrySet()) {

                    generatedObj.getMethod("set" + prop.getKey(), prop.getValue()).invoke(obj,
                            cellIterator.next().getStringCellValue());
                }
                obj.setRowId(rowNumber);
                excelObjs.add(obj);
                log.debug("added object (" + obj + ") by row( " + rowNumber + ")");
                rowNumber++;
            }
        }

    }

    private SortedMap<String, Class<?>> getColumnNames(final HSSFRow firstRow, final HSSFRow secondRow) {

        int colNum = firstRow.getPhysicalNumberOfCells();

        SortedMap<String, Class<?>> colMapByName = new TreeMap<>();

        if (firstRow.cellIterator().hasNext()) {
            //Add rowId
            colMapByName.put("rowId", Integer.class);

            for (int j = 0; j < colNum; j++) {
                HSSFCell cell = firstRow.getCell(j);
                if (cell != null) {
                    colMapByName.put(getColumnNameByCellValue(cell.getStringCellValue()), getCellClass(secondRow.getCell(j)));
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

    private Class<?> getCellClass(HSSFCell cell) {

        if (cell.getCellType().equals(CellType.STRING)) {
            if (isDateCell(cell.getStringCellValue())) {
                return Date.class;
            }
            return String.class;
        } else if (cell.getCellType().equals(CellType.NUMERIC)) {
            return Integer.class;
        } else {
            return String.class;
        }
    }

    private boolean isDateCell(String stringCellValue) {
        String dateRegex = "^[0-3]?[0-9]/[0-3]?[0-9]/(?:[0-9]{2})?[0-9]{2}$";

        Pattern pattern = Pattern.compile(dateRegex);
        return pattern.matcher(dateRegex).matches();
    }

}
