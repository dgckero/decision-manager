/*
  @author david
 */

package com.dgc.dm.core.service.db;

import com.dgc.dm.core.db.dao.DataDao;
import com.dgc.dm.core.db.model.Project;
import com.dgc.dm.core.dto.ProjectDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class DataServiceImpl extends CommonServer implements DataService {

    @Autowired
    private DataDao dataDao;

    @Override
    public final void createDataTable(Map<String, Class<?>> columns, ProjectDto project) {
        dataDao.createDataTable(columns, getModelMapper().map(project, Project.class));
    }

    @Override
    public final void persistData(String insertSentence, List<Object[]> infoToBePersisted) {
        log.info("Persisting Excel rows");
        dataDao.persistData(insertSentence, infoToBePersisted);
        log.info("Persisted Excel rows");
    }

    @Override
    public final List<Map<String, Object>> getCommonData(ProjectDto project) {
        log.info("Getting all info from table: {}", project.getCommonDataTableName());
        List<Map<String, Object>> entities = dataDao.getCommonData(getModelMapper().map(project, Project.class));
        log.info("Got all info from table: {}", project.getCommonDataTableName());
        return entities;
    }

    @Override
    public final void deleteCommonData(ProjectDto project) {
        log.debug("Deleting all registers for project {}", project);
        dataDao.deleteCommonData(getModelMapper().map(project, Project.class));
        log.debug("Registers successfully deleted for project {}", project);
    }

    @Override
    public final Integer getCommonDataSize(ProjectDto project) {
        log.debug("Getting common data size for project {}", project);
        Integer commonDataSize = dataDao.getCommonDataSize(getModelMapper().map(project, Project.class));
        log.debug("common data size {} for project {}", commonDataSize, project);
        return commonDataSize;
    }
}
