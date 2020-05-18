/*
  @author david
 */

package com.dgc.dm.web.facade;

import com.dgc.dm.core.service.db.FilterService;
import com.dgc.dm.core.service.db.ProjectService;
import com.dgc.dm.core.service.db.RowDataService;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;

@Getter(AccessLevel.PROTECTED)
@Setter(AccessLevel.PROTECTED)
class CommonFacade {

    @Autowired
    private RowDataService rowDataService;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private FilterService filterService;
}
