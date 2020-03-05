/**
 * @author david
 */

package com.dgc.dm.core.db.service;

import java.util.List;
import java.util.Map;

public interface DbServer {
    void createAndPopulateFilterTable(final Map<String, Class<?>> props);

    void persistExcelRows(final String insertSentence, final List<Object[]> infoToBePersisted);

    void getFilters();
}
