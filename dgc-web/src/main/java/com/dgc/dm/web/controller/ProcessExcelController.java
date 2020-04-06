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
    public ModelAndView resolveException(final HttpServletRequest request, final HttpServletResponse response,
                                         final Object object, final Exception exc) {
        final ModelAndView modelAndView = new ModelAndView("file");
        if (exc instanceof MaxUploadSizeExceededException) {
            modelAndView.getModel().put("message", "File size exceeds limit!");
        }
        return modelAndView;
    }

    @RequestMapping(value = "/uploadFile", method = RequestMethod.POST)
    public ModelAndView uploadFile(@RequestParam("projectName") final String projectName, @RequestParam("file") final MultipartFile file) {

        log.info("processing file " + file.getOriginalFilename() + " for " + projectName);

        final ModelAndView modelAndView = new ModelAndView("decision");
        final ProjectDto project = this.createProject(file, projectName);
        final List<Object> excelObjs = this.processExcel(file, project);

        log.info("processed (" + excelObjs.size() + ") rows");

        modelAndView.getModel().put("project", project);
        modelAndView.getModel().put("message", "File uploaded successfully!");

        final Map<String, List<Map<String, Object>>> filterList = this.getModelMapWithFilters(project);
//TODO to be optimized
        final List<FilterDto> filterDtoList = new ArrayList<>();

        FilterDto contact = null;

        final Iterator<Map<String, Object>> entryIterator = filterList.get("filterList").iterator();
        while (entryIterator.hasNext()) {
            final Map<String, Object> filterIterator = entryIterator.next();

            final String filterName = (String) filterIterator.get("name");
            if (filterName.equals("rowId")) {
                // Don't send rowId to decision view
                entryIterator.remove();
            } else {
                final FilterDto filter = FilterDto.builder().
                        id((Integer) filterIterator.get("ID")).
                        name(filterName).
                        filterClass((String) filterIterator.get("class")).
                        contactFilter(filterIterator.get("contactFilter").equals(CONTACT_FILTER)).
                        project(project).
                        build();

                filterDtoList.add(filter);

                if (filter.getContactFilter() != null && filter.getContactFilter().equals(Boolean.TRUE)) {
                    contact = filter;
                }
            }
        }
//TODO END to be optimized
        modelAndView.addAllObjects(filterList);
        final FilterCreationDto form = new FilterCreationDto(filterDtoList);
        modelAndView.addObject("form", form);
        modelAndView.addObject("contactFilter", contact);

        return modelAndView;
    }

    private Map<String, List<Map<String, Object>>> getModelMapWithFilters(final ProjectDto project) {
        final List<Map<String, Object>> filters = this.dbServer.getFilters(project);

        final Map<String, List<Map<String, Object>>> modelMap = new HashMap<>();
        modelMap.put("filterList", filters);

        return modelMap;
    }

    private ProjectDto createProject(MultipartFile file, String projectName) {
        log.info("Creating project " + projectName);
        final Project project = this.dbServer.createProject(projectName);
        log.info("Creating project " + projectName);
        return this.modelMapper.map(project, ProjectDto.class);
    }

    private List<Object> processExcel(MultipartFile file, ProjectDto project) {

        final List<Object> excelObjs = new ArrayList<>();
        try {
            HSSFSheet worksheet = this.getWorkSheet(file);

            log.info("Getting Excel's column names");
            final Map<String, Class<?>> colMapByName = this.getColumnNames(worksheet);

            final List<Filter> filterList = this.dbServer.createCommonDatasTable(colMapByName, project);

            this.processExcelRows(worksheet, colMapByName, excelObjs, project);

            this.persistFilterList(filterList, project);

        } catch (final Exception e) {
            log.error("Error " + e.getMessage());
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
    private void persistFilterList(List<Filter> filterList, ProjectDto project) {
        this.dbServer.createFilterTable(project);

        filterList = markContactFilter(filterList);

        this.dbServer.persistFilterList(filterList);
    }

    private List<Filter> markContactFilter(List<Filter> filterList) {
        AtomicInteger itemNumber = new AtomicInteger();

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
            this.dbServer.persistExcelRows(this.getInsertSentence(columns), infoToBePersisted);
        } else {
            log.error("No columns found on File");
        }
    }

    private String getInsertSentence(Map<String, Class<?>> columns) {
        StringBuilder insertQuery = new StringBuilder("insert into commonDatas (");

        for (final Map.Entry<String, Class<?>> column : columns.entrySet()) {
            insertQuery.append(column.getKey()).append(",");
        }
        insertQuery = new StringBuilder(insertQuery.toString().replaceAll("[,]$", ", project, rowId) "));

        insertQuery.append(" values(").append(new String(new char[columns.size() + 2]).replace("\0", "?,"));

        insertQuery = new StringBuilder(insertQuery.toString().replaceAll("[,]$", ")"));

        return insertQuery.toString();
    }

    private HSSFSheet getWorkSheet(MultipartFile file) throws IOException {
        log.info("Getting workSheet from file " + file.getName());
        HSSFWorkbook workbook = new HSSFWorkbook(file.getInputStream());
        return workbook.getSheetAt(SHEET_ZERO);
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

    private LinkedHashMap<String, Class<?>> getColumnNames(HSSFSheet worksheet) {

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
                    log.info("Processed column(" + j + "), columnName " + cell.getStringCellValue() + ", class " + ((cellClass == null) ? "NULL" : cellClass.getName()));
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
