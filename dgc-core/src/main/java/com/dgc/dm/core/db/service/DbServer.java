/**
 * @author david
 */

package com.dgc.dm.core.db.service;

import java.util.Map;

public interface DbServer {
    void createFilterTable(Map<String, Class<?>> props);
}
