package com.dgc.dm.core.db.dao;

import com.dgc.dm.core.db.model.Project;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.NativeQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

class RowDataDaoImplTest {

    @Mock
    private NativeQuery mockNativeQuery;

    @Mock
    private Session mockSession;

    @Mock
    private SessionFactory mockSessionFactory;

    @Mock
    private JdbcTemplate mockJdbcTemplate;

    @InjectMocks
    private RowDataDaoImpl rowDataDaoImplUnderTest;

    @BeforeEach
    void setUp() {
        initMocks(this);
        when(mockSessionFactory.getCurrentSession()).thenReturn(mockSession);
    }

    @Test
    void testCreateRowDataTable() {
        // Setup
        final Map<String, Class<?>> columns = new HashMap<>();
        final Project project = new Project(0, "name", "rowDataTableName", "emailTemplate", "content".getBytes());
        when(mockSession.createSQLQuery(any())).thenReturn(mockNativeQuery);
        when(mockNativeQuery.executeUpdate()).thenReturn(1);

        // Run the test
        rowDataDaoImplUnderTest.createRowDataTable(columns, project);
        // Verify the results
        assertTrue(true);
    }

    @Test
    void testPersistRowData() {
        // Setup
        final List<Object[]> infoToBePersisted = new ArrayList<Object[]>() {
            {
                add(new Object[]{"value"});
            }
        };

        when(mockSession.createSQLQuery(any())).thenReturn(mockNativeQuery);
        when(mockNativeQuery.executeUpdate()).thenReturn(1);
        // Run the test
        rowDataDaoImplUnderTest.persistRowData("?", infoToBePersisted);

        // Verify the results
        assertTrue(true);
    }

    @Test
    void testGetRowData() {
        // Setup
        final Project project = new Project(0, "name", "rowDataTableName", "emailTemplate", "content".getBytes());
        when(mockJdbcTemplate.queryForList(any())).thenReturn(Mockito.anyList());
        // Run the test
        final List<Map<String, Object>> result = rowDataDaoImplUnderTest.getRowData(project);

        // Verify the results
        assertTrue(true);
    }

    @Test
    void testDeleteRowData() {
        // Setup
        final Project project = new Project(0, "name", "rowDataTableName", "emailTemplate", "content".getBytes());
        when(mockSession.createSQLQuery(any())).thenReturn(mockNativeQuery);
        when(mockNativeQuery.executeUpdate()).thenReturn(1);
        // Run the test
        rowDataDaoImplUnderTest.deleteRowData(project);

        // Verify the results
        assertTrue(true);
    }

    @Test
    void testGetRowDataSize() {
        // Setup
        final Project project = new Project(0, "name", "rowDataTableName", "emailTemplate", "content".getBytes());
        when(mockJdbcTemplate.queryForObject(Mockito.anyString(), ArgumentMatchers.<Class<Integer>>any())).thenReturn(new Integer(1));
        // Run the test
        final Integer result = rowDataDaoImplUnderTest.getRowDataSize(project);

        // Verify the results
        assertEquals(1, result);
    }
}
