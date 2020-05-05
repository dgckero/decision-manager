/*
  @author david
 */

package com.dgc.dm.core.service.db;

import com.dgc.dm.core.db.dao.RowDataDao;
import com.dgc.dm.core.db.model.Project;
import com.dgc.dm.core.dto.ProjectDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@Transactional
public class RowDataServiceImpl extends CommonServer implements RowDataService {

    @Autowired
    private RowDataDao rowDataDao;

    /**
     * Create RowDataTable based on project.RowDataTableName
     *
     * @param columns
     * @param project
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public final void createRowDataTable (Map<String, Class<?>> columns, ProjectDto project) {
        log.debug("[INIT] createRowDataTable project: {}", project);
        rowDataDao.createRowDataTable(columns, getModelMapper().map(project, Project.class));
        log.debug("[END] createRowDataTable");
    }

    /**
     * Persist rowData
     *
     * @param insertSentence
     * @param infoToBePersisted
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public final void persistRowData (String insertSentence, List<Object[]> infoToBePersisted) {
        log.debug("[INIT] Persisting Excel rows");
        rowDataDao.persistRowData(insertSentence, infoToBePersisted);
        log.debug("[END] Persisted Excel rows");
    }

    /**
     * get Row Data by project
     *
     * @param project
     * @return List of RowData
     */
    @Override
    public final List<Map<String, Object>> getRowData (ProjectDto project) {
        log.debug("[INIT] Getting all info from table: {}", project.getRowDataTableName());
        List<Map<String, Object>> entities = rowDataDao.getRowData(getModelMapper().map(project, Project.class));
        log.info("[END] Got all info from table: {}", project.getRowDataTableName());
        return entities;
    }

    /**
     * Delete all row data by project
     *
     * @param project
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public final void deleteRowData (ProjectDto project) {
        log.debug("[INIT] Deleting all registers for project {}", project);
        rowDataDao.deleteRowData(getModelMapper().map(project, Project.class));
        log.debug("[END] Registers successfully deleted for project {}", project);
    }

    /**
     * Get number of rows on table project.RowDataTableName
     *
     * @param project
     * @return number of rows on table project.RowDataTableName
     */
    @Override
    public final Integer getRowDataSize (ProjectDto project) {
        log.debug("[INIT] Getting common data size for project {}", project);
        Integer commonDataSize = rowDataDao.getRowDataSize(getModelMapper().map(project, Project.class));
        log.debug("[END] common data size {} for project {}", commonDataSize, project);
        return commonDataSize;
    }
}
