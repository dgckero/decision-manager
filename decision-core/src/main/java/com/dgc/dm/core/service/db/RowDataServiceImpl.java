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
    public final void createRowDataTable (final Map<String, Class<?>> columns, final ProjectDto project) {
        log.debug("[INIT] createRowDataTable project: {}", project);
        this.rowDataDao.createRowDataTable(columns, this.getModelMapper().map(project, Project.class));
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
    public final void persistRowData (final String insertSentence, final List<Object[]> infoToBePersisted) {
        log.debug("[INIT] Persisting Excel rows");
        this.rowDataDao.persistRowData(insertSentence, infoToBePersisted);
        log.debug("[END] Persisted Excel rows");
    }

    /**
     * get Row Data by project
     *
     * @param project
     * @return List of RowData
     */
    @Override
    public final List<Map<String, Object>> getRowData (final ProjectDto project) {
        log.debug("[INIT] Getting all info from table: {}", project.getRowDataTableName());
        final List<Map<String, Object>> entities = this.rowDataDao.getRowData(this.getModelMapper().map(project, Project.class));
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
    public final void deleteRowData (final ProjectDto project) {
        log.debug("[INIT] Deleting all registers for project {}", project);
        this.rowDataDao.deleteRowData(this.getModelMapper().map(project, Project.class));
        log.debug("[END] Registers successfully deleted for project {}", project);
    }

    /**
     * Get number of rows on table project.RowDataTableName
     *
     * @param project
     * @return number of rows on table project.RowDataTableName
     */
    @Override
    public final Integer getRowDataSize (final ProjectDto project) {
        log.debug("[INIT] Getting common data size for project {}", project);
        final Integer commonDataSize = this.rowDataDao.getRowDataSize(this.getModelMapper().map(project, Project.class));
        log.debug("[END] common data size {} for project {}", commonDataSize, project);
        return commonDataSize;
    }
}
