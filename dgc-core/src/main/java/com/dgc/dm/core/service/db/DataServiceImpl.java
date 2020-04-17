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
    public void createDataTable(Map<String, Class<?>> columns, ProjectDto project) {

        dataDao.createDataTable(columns, getModelMapper().map(project, Project.class));
    }

    @Override
    public void persistData(String insertSentence, List<Object[]> infoToBePersisted) {
        log.info("Persisting Excel rows");
        dataDao.persistData(insertSentence, infoToBePersisted);
        log.info("Persisted Excel rows");
    }
}
