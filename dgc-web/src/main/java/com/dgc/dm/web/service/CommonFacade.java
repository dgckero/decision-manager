/*
  @author david
 */

package com.dgc.dm.web.service;

import com.dgc.dm.core.service.db.DataService;
import com.dgc.dm.core.service.db.FilterService;
import com.dgc.dm.core.service.db.ProjectService;
import lombok.AccessLevel;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;

@Getter(AccessLevel.PROTECTED)
public class CommonFacade {

    @Autowired
    private DataService dataService;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private FilterService filterService;
}
