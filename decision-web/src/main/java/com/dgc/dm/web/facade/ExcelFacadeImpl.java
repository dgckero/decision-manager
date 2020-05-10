/*
  @author david
 */

package com.dgc.dm.web.facade;

import com.dgc.dm.core.dto.FilterDto;
import com.dgc.dm.core.dto.ProjectDto;
import com.dgc.dm.core.dto.RowDataDto;
import com.dgc.dm.core.exception.DecisionException;
import com.dgc.dm.core.generator.PojoGenerator;
import javassist.CannotCompileException;
import javassist.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.hibernate.exception.GenericJDBCException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.sqlite.SQLiteErrorCode;

import javax.persistence.PersistenceException;
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

    /**
     * Generate filter list based on project's columns
     *
     * @param columns
     * @param project
     * @return
     */
    private static List<FilterDto> generateFilterList (final Map<String, Class<?>> columns, final ProjectDto project) {
        log.info("[INIT] generateFilterList by project: {}", project);

        final List<FilterDto> filterList = new ArrayList<>();
        for (final Map.Entry<String, Class<?>> column : columns.entrySet()) {
            if (!StringUtils.isEmpty(column.getValue()) && column.getValue() != null) {
                filterList.add(FilterDto.builder().
                        name(column.getKey()).
                        filterClass(column.getValue().getSimpleName()).
                        active(Boolean.FALSE).
                        contactFilter(Boolean.FALSE).
                        project(project).
                        build());
            }
        }
        log.info("[END] generateFilterList by project: {}", project);
        return filterList;
    }

    /**
     * Generate insert sentences to project.rowDataTableName
     *
     * @param columns
     * @param rowDataTableName
     * @return inserts
     */
    private static String getInsertSentence (final Map<String, Class<?>> columns, final String rowDataTableName) {
        log.debug("[INIT] getInsertSentence to rowDataTableName: {}", rowDataTableName);
        StringBuilder insertQuery = new StringBuilder("insert into " + rowDataTableName + " (");

        for (final Map.Entry<String, Class<?>> column : columns.entrySet()) {
            insertQuery.append(column.getKey()).append(",");
        }
        insertQuery = new StringBuilder(insertQuery.toString().replaceAll("[,]$", ", project, rowId, dataCreationDate) "));
        insertQuery.append(" values(").append(new String(new char[columns.size() + 3]).replace("\0", "?,"));
        insertQuery = new StringBuilder(insertQuery.toString().replaceAll("[,]$", ")"));

        log.debug("[END] getInsertSentence to rowDataTableName: {}", rowDataTableName);
        return insertQuery.toString();
    }

    /**
     * check if array is empty
     *
     * @param array
     * @return true if array is empty
     */
    private static boolean isArrayEmpty (final Object[] array) {
        log.debug("[INIT] isArrayEmpty");
        boolean isEmpty = true;
        for (final Object ob : array) {
            if (null != ob) {
                isEmpty = false;
            }
        }
        log.debug("[INIT] isArrayEmpty, isEmpty: {}", isEmpty);
        return isEmpty;
    }

    /**
     * Add newObj to obj array
     *
     * @param obj
     * @param newObj
     * @return array
     */
    private static Object[] appendValueToObjectArray (final Object[] obj, final Object newObj) {
        log.debug("[INIT] appendValueToObjectArray");
        final List<Object> temp = new ArrayList<>(Arrays.asList(obj));
        temp.add(newObj);
        log.debug("[END] appendValueToObjectArray");
        return temp.toArray();
    }

    /**
     * Get work Sheet by Excel file
     *
     * @param file
     * @return
     * @throws IOException
     */
    private static Sheet getWorkSheet (final MultipartFile file) throws IOException {
        log.info("[INIT] Getting workSheet from file {}", file.getName());
        final Workbook workbook = WorkbookFactory.create(file.getInputStream());
        log.info("[END] Getting workSheet from file {}", file.getName());
        return workbook.getSheetAt(SHEET_ZERO);
    }

    /**
     * Get column name by cell value
     *
     * @param value
     * @return column name
     */
    private static String getColumnNameByCellValue (final String value) {
        log.debug("[INIT] getColumnNameByCellValue value: {}", value);
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
        final String result = sb.toString().replaceAll("\\s+", "").replaceAll("_", "");
        log.debug("[END] getColumnNameByCellValue result: {}", result);
        return result;
    }

    /**
     * Get next not empty cell by column
     *
     * @param cell
     * @param worksheet
     * @param cellNumber
     * @param goOverWorkSheet
     * @return cell
     */
    private static Cell getNextCellNoNull (final Cell cell, final Sheet worksheet, final int cellNumber, final boolean goOverWorkSheet) {
        log.debug("[INIT] getNextCellNoNull cellNumber: {}, goOverWorkSheet:{}", cellNumber, goOverWorkSheet);
        Cell cellToBeProcessed = cell;
        if (null == cell) {
            if (goOverWorkSheet) {
                for (int i = 2; i < worksheet.getLastRowNum() && null == cellToBeProcessed; i++) {
                    final Row row = worksheet.getRow(i);
                    cellToBeProcessed = row.getCell(cellNumber);
                }
            }
        }
        log.debug("[END] getNextCellNoNull cellToBeProcessed: {}", cellToBeProcessed);
        return cellToBeProcessed;
    }

    /**
     * check if cell is type date
     *
     * @param cell
     * @return true if cellType is Date
     */
    private static boolean isDateCell (final Cell cell) {
        try {
            return DateUtil.isCellDateFormatted(cell);
        } catch (final java.lang.IllegalStateException e) {
            //Nothing to do
            return false;
        }
    }

    /**
     * Compares if the columns previously registered for the project match the columns of the new file
     *
     * @param worksheet
     * @param file
     * @param project
     * @return Excel's columns
     */
    private Map<String, Class<?>> compareExcelColumnNames (final Sheet worksheet, final MultipartFile file, final ProjectDto project) {
        log.debug("[INIT] compareExcelColumnNames for project: {}", project);
        final Map<String, Class<?>> colMapByName = this.getExcelColumnNames(worksheet);
        if (colMapByName.isEmpty()) {
            log.warn("[END] No columns found on file {}", file.getOriginalFilename());
            throw new DecisionException("El Excel " + file.getOriginalFilename() + " no tiene columnas");
        }
        log.debug("Found {} filters on excel {}", colMapByName.size(), file.getOriginalFilename());
        final List<Map<String, Object>> projectFilters = this.getFilterService().getFilters(project);
        if (null == projectFilters || projectFilters.isEmpty()) {
            log.warn("[END] No filters found for project {}", project);
            throw new DecisionException("No se han encontrado filtros para el proyecto " + project.getName());
        } else if ((projectFilters.size() - 1) == colMapByName.size()) { // subtract 1 to project's filters because of the rowId column
            log.info("[END] Excel file has same number of columns ({}) than filters ({}) for project {}", colMapByName.size(), projectFilters.size() - 1, project);
            return colMapByName;
        } else {
            log.warn("[END] Excel file has NOT same number of columns ({}) than filters ({}) for project {}", colMapByName.size(), projectFilters.size(), project);
            throw new DecisionException("El nuevo Excel no tiene el mismo número de columnas (" + colMapByName.size() + ") que las definidas para el proyecto original (" + projectFilters.size() + ") en el proyecto " + project.getName());
        }

    }

    /**
     * Process Excel file, updates project model and get columns defined on it
     *
     * @param file
     * @param project
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public final void processExcel (final MultipartFile file, final ProjectDto project) {
        log.info("[INIT] Processing Excel file: {}, for project: {} ", file.getOriginalFilename(), project);

        try {
            final Sheet worksheet = getWorkSheet(file);
            final Map<String, Class<?>> colMapByName = this.compareExcelColumnNames(worksheet, file, project);
            this.processExcelRows(worksheet, colMapByName, project, this.getRowDataService().getRowDataSize(project) + 1);
            log.info("[END] Processed Excel file " + file.getOriginalFilename());
        } catch (final DecisionException e) {
            throw e;
        } catch (final Exception e) {
            log.error("Error processing Excel " + e.getMessage());
            e.printStackTrace();
            throw new DecisionException("Error procesando el Excel: " + file.getName() + " para el proyecto: " + project.getName());
        }
    }

    /**
     * Process Excel file, updates project model and get columns defined on it
     *
     * @param file
     * @param projectId
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public final ProjectDto processExcel (final MultipartFile file, final Integer projectId) {
        log.info("[INIT] Processing Excel file: {}, for projectId: {} ", file.getOriginalFilename(), projectId);

        ProjectDto project = getProjectService().getProject(projectId);
        if (null == project) {
            log.error("No project found by id: {}", projectId);
        } else {
            try {
                log.info("found project: {} ", project);
                final Sheet worksheet = getWorkSheet(file);
                final Map<String, Class<?>> colMapByName = this.compareExcelColumnNames(worksheet, file, project);
                this.processExcelRows(worksheet, colMapByName, project, this.getRowDataService().getRowDataSize(project) + 1);
            } catch (final DecisionException e) {
                throw e;
            } catch (final Exception e) {
                log.error("Error processing Excel " + e.getMessage());
                e.printStackTrace();
                throw new DecisionException("Error procesando el Excel file: " + file.getName() + " para el proyecto: " + project.getName());
            }
        }
        log.info("[END] Processed Excel file " + file.getOriginalFilename());
        return project;
    }

    /**
     * Process Excel file and create project model
     *
     * @param file
     * @param projectName
     * @return project model
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public final ProjectDto processExcel (final MultipartFile file, final String projectName) throws IOException {
        log.info("[INIT] Processing Excel file");

        final Sheet worksheet = getWorkSheet(file);
        final Map<String, Class<?>> colMapByName = this.getExcelColumnNames(worksheet);
        final ProjectDto project = this.createProjectModel(projectName, colMapByName);
        this.processExcelRows(worksheet, colMapByName, project, 0);

        log.info("[END] Processing Excel file");
        return project;
    }

    /**
     * Create Project and Data tables
     * Add Project information on Project table
     *
     * @param projectName
     * @param colMapByName
     * @return new Project
     */
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    private ProjectDto createProjectModel (final String projectName, final Map<String, Class<?>> colMapByName) {
        log.info("[INIT] createProjectModel by projectName: {}", projectName);
        ProjectDto project = null;
        try {
            project = this.getProjectService().createProject(projectName);
            this.getRowDataService().createRowDataTable(colMapByName, project);
            this.getFilterService().persistFilterList(generateFilterList(colMapByName, project), project);
        } catch (final PersistenceException e) {
            log.error("Error Creating project : {}", e);
            if (e.getCause() instanceof GenericJDBCException && ((GenericJDBCException) e).getErrorCode() == SQLiteErrorCode.SQLITE_CONSTRAINT.code) {
                throw new DecisionException("Ya existe un proyecto con nombre " + projectName + ", por favor utilice un nombre diferente");
            }
        }
        log.info("[END] createProjectModel project: {}", project);
        return project;
    }

    /**
     * Generate dynamic class, populate it with Excel's row and persist it on project.rowDataTableName
     *
     * @param worksheet
     * @param columns
     * @param project
     * @param rowIdNumber
     * @throws IOException
     * @throws CannotCompileException
     * @throws NotFoundException
     * @throws InvocationTargetException
     * @throws NoSuchMethodException
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    private void processExcelRows (final Sheet worksheet, final Map<String, Class<?>> columns, final ProjectDto project, int rowIdNumber) {
        log.info("[INIT] processExcelRows by project: {}, rowIdNumber: {}", project, rowIdNumber);
        if (null != columns && !columns.isEmpty()) {
            final List<Object> excelObjs = new ArrayList<>();

            log.info("Generating dynamic class");
            Class<? extends RowDataDto> generatedObj = null;
            try {
                generatedObj = PojoGenerator.generate("com.dgc.dm.core.dto.Pojo" + project.getName() + "$Generated", columns);
                log.info("Generated dynamic class: {}", generatedObj.getName());

                log.info("Populating generated dynamic class with Excel's row values");
                final List<Object[]> infoToBePersisted = new ArrayList<>();
                for (int rowNumber = 1; rowNumber < worksheet.getPhysicalNumberOfRows(); rowNumber++, rowIdNumber++) {
                    final Object[] info = this.populateGeneratedObject(project, worksheet.getRow(rowNumber), generatedObj, columns, excelObjs, rowIdNumber);
                    if (null != info) {
                        infoToBePersisted.add(info);
                    }
                }
                this.getRowDataService().persistRowData(getInsertSentence(columns, project.getRowDataTableName()), infoToBePersisted);

                log.info("processed ({}) rows", excelObjs.size());
            } catch (final Exception e) {
                log.error("Error processing Excel " + e.getMessage());
                e.printStackTrace();
                throw new DecisionException("Error procesando la fila " + rowIdNumber + " del Excel para el proyecto: " + project.getName() + ", Por favor revise esta fila");
            }
        } else {
            log.error("No columns found on File");
        }
        log.info("[END] processExcelRows by project: {}, rowIdNumber: {}", project, rowIdNumber);
    }

    /**
     * Populated dynamic class with row information
     *
     * @param project
     * @param row
     * @param generatedObj
     * @param columns
     * @param excelObjs
     * @param rowNumber
     * @return
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     */
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    private Object[] populateGeneratedObject (final ProjectDto project, final Row row, final Class<? extends RowDataDto> generatedObj, final Map<String, Class<?>> columns,
                                              final List<Object> excelObjs, final int rowNumber) throws IllegalAccessException,
            InstantiationException, NoSuchMethodException, InvocationTargetException {

        log.trace("[INIT] populating dynamic class with Excel row number {}", rowNumber);
        Object[] insertQueryValues = {};
        final Iterator<Cell> excelRowIterator = row.cellIterator();
        while (excelRowIterator.hasNext()) {
            final RowDataDto obj = generatedObj.getConstructor().newInstance();
            for (final Map.Entry<String, Class<?>> column : columns.entrySet()) {
                if (column.getValue() == Date.class) {
                    column.setValue(String.class);
                }
                insertQueryValues = appendValueToObjectArray(insertQueryValues, this.populateDynamicClassProperty(generatedObj, column, excelRowIterator, obj));
            }
            //Add projectId
            insertQueryValues = appendValueToObjectArray(insertQueryValues, project.getId());
            if (isArrayEmpty(insertQueryValues)) {
                insertQueryValues = new Object[]{};
            } else {
                //Add rowId
                insertQueryValues = appendValueToObjectArray(insertQueryValues, rowNumber);
                // Add createDate
                insertQueryValues = appendValueToObjectArray(insertQueryValues, new Date());

                obj.setRowId(rowNumber);
                obj.setProject(project);
                obj.setDataCreationDate(this.format.format(new Date()));
                excelObjs.add(obj);

                log.trace("added object ({}) by row( {})", obj, rowNumber);
            }
        }
        if (isArrayEmpty(insertQueryValues)) {
            log.warn("insertQueryValues is empty");
            return null;
        }
        log.trace("[END] populating dynamic class with Excel row number {}", rowNumber);
        return insertQueryValues;
    }

    /**
     * Populate dynamic's class property with column data
     *
     * @param generatedObj
     * @param column
     * @param excelRowIterator
     * @param obj
     * @return
     * @throws NoSuchMethodException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    private String populateDynamicClassProperty (final Class<? extends RowDataDto> generatedObj, final Map.Entry<String, Class<?>> column,
                                                 final Iterator<? extends Cell> excelRowIterator, final RowDataDto obj) {
        log.trace("[INIT] Populating property {} with value {}", column.getKey(), column.getValue());
        final String result;
        final String columnName = PojoGenerator.getPropertyNameByColumnName(column.getKey());
        final String setMethod = "set" + StringUtils.capitalize(columnName);
        if (excelRowIterator.hasNext()) {
            String cellValue = null;
            try {
                cellValue = this.populateMethodParameter(setMethod, generatedObj, column.getValue(), excelRowIterator.next(), obj);
                log.trace("Populated property {} with value {}", columnName, cellValue);
                result = cellValue;
            } catch (final Exception e) {
                log.error("Populated property: " + columnName + " with value" + cellValue + ",error: " + e.getMessage());
                e.printStackTrace();
                throw new DecisionException("Error procesando el Excel, columna: " + columnName + " con valor: " + column.getValue());
            }
        } else {
            log.trace("Populated property {} with null value ", columnName);
            result = null;
        }
        log.trace("[END] Populating property, result {}", result);
        return result;
    }

    /**
     * Populate method parameter
     *
     * @param setMethod
     * @param generatedObj
     * @param columnClass
     * @param cell
     * @param obj
     * @return getMethod
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    private String populateMethodParameter (final String setMethod, final Class<? extends RowDataDto> generatedObj, final Class<?> columnClass, final Cell cell, final RowDataDto obj) {
        final String result;
        log.debug("[INIT] populateMethodParameter setMethod {}, columnClass {} ", setMethod, columnClass);
        final Class<?> cellClass = this.getCellClass(cell);
        if (null == cellClass) {
            log.trace("cell is BLANK");
            result = null;
        } else {
            log.trace("Populating method: {}, class: {}, value: {}", setMethod, cellClass.getName(), cell);
            final Class<?> colClass = (columnClass.equals(Email.class)) ? String.class : columnClass;
            try {
                if (cellClass.isAssignableFrom(Date.class)) {
                    final String cellValue = this.format.format(cell.getDateCellValue());
                    generatedObj.getMethod(setMethod, colClass).invoke(obj, cellValue);
                    result = cellValue;
                } else if (cellClass.isAssignableFrom(Double.class)) {
                    generatedObj.getMethod(setMethod, colClass).invoke(obj,
                            cell.getNumericCellValue());
                    result = cell.getNumericCellValue() + "";
                } else {
                    String cellValue =
                            (null == cell.getStringCellValue()
                                    ||
                                    (StringUtils.isEmpty(cell.getStringCellValue().replaceAll("[^\\p{Alpha}\\p{Digit}]+", ""))))
                                    ? null :
                                    cell.getStringCellValue();
                    log.debug("generatedObj.getMethod setMethod: {}, colClass: {}, cellValue: {}", setMethod, colClass, cellValue);
                    generatedObj.getMethod(setMethod, colClass).invoke(obj, cellValue);
                    result = cellValue;
                }
            } catch (final Exception e) {
                log.error("Error populateMethodParameter " + e.getMessage());
                e.printStackTrace();
                throw new DecisionException("Error procesando el Excel, metodo: " + setMethod + ", celda: " + cell + ", clase de campo: " + cellClass);
            }
        }
        log.debug("[END] populateMethodParameter result {} ", result);
        return result;
    }

    /**
     * Get Excel column names
     *
     * @param worksheet
     * @return Map of column names
     */
    private Map<String, Class<?>> getExcelColumnNames (final Sheet worksheet) {
        log.info("[INIT] Getting Excel's column names");
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
                    final String columnName = cell.getStringCellValue();
                    if (!StringUtils.isEmpty(columnName) && (null != cellClass)) {
                        colMapByName.put(getColumnNameByCellValue(columnName), cellClass);
                        log.trace("Processed column({}), columnName {}, class {}", j, columnName, (null == cellClass) ? "NULL" : cellClass.getName());
                    }
                }
            }
        }
        log.info("[END] Getting Excel's column names");
        return colMapByName;
    }

    /**
     * Get cell class
     *
     * @param cell
     * @return cell class
     */
    private Class<?> getCellClass (final Cell cell) {
        return this.getCellClass(cell, null, 0, false);
    }

    /**
     * Get cell class based on parameters
     *
     * @param cell
     * @param worksheet
     * @param cellNumber
     * @param goOverWorkSheet
     * @return
     */
    private Class<?> getCellClass (final Cell cell, final Sheet worksheet, final int cellNumber, final boolean goOverWorkSheet) {
        Class<?> result = String.class;
        log.debug("[INIT] getCellClass cellNumber: {}, goOverWorkSheet: {}", cellNumber, goOverWorkSheet);
        final Cell cellToBeProcessed = getNextCellNoNull(cell, worksheet, cellNumber, goOverWorkSheet);

        if (null == cellToBeProcessed) {
            log.warn("Column number {} is null in all sheet", cellNumber);
        } else {
            switch (cellToBeProcessed.getCellType()) {
                case STRING:
                    if (cellToBeProcessed.getStringCellValue().contains("@")) {
                        result = Email.class;
                        break;
                    } else if (isDateCell(cellToBeProcessed)) {
                        result = Date.class;
                        break;
                    }
                    break;
                case NUMERIC:
                    if (isDateCell(cellToBeProcessed)) {
                        result = Date.class;
                        break;
                    }
                    result = Double.class;
                    break;
                case BLANK:
                    result = null;
                    break;
                default:
                    break;
            }
        }
        log.debug("[INIT] getCellClass result: {}", result);
        return result;
    }
}
