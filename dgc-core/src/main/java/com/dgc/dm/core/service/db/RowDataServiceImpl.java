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

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public final void createRowDataTable(Map<String, Class<?>> columns, ProjectDto project) {
        rowDataDao.createRowDataTable(columns, getModelMapper().map(project, Project.class));
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public final void persistRowData(String insertSentence, List<Object[]> infoToBePersisted) {
        log.info("Persisting Excel rows");
        rowDataDao.persistRowData(insertSentence, infoToBePersisted);
        log.info("Persisted Excel rows");
    }

    @Override
    public final List<Map<String, Object>> getRowData(ProjectDto project) {
        log.info("Getting all info from table: {}", project.getRowDataTableName());
        List<Map<String, Object>> entities = rowDataDao.getRowData(getModelMapper().map(project, Project.class));
        log.info("Got all info from table: {}", project.getRowDataTableName());
        return entities;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public final void deleteRowData(ProjectDto project) {
        log.debug("Deleting all registers for project {}", project);
        rowDataDao.deleteRowData(getModelMapper().map(project, Project.class));
        log.debug("Registers successfully deleted for project {}", project);
    }

    @Override
    public final Integer getRowDataSize(ProjectDto project) {
        log.debug("Getting common data size for project {}", project);
        Integer commonDataSize = rowDataDao.getRowDataSize(getModelMapper().map(project, Project.class));
        log.debug("common data size {} for project {}", commonDataSize, project);
        return commonDataSize;
    }
}
