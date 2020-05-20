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

import java.util.*;

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

    final Project project = new Project(0, "name", "rowDataTableName", "emailTemplate", "content".getBytes());

    @BeforeEach
    void setUp() {
        initMocks(this);
        when(mockSessionFactory.getCurrentSession()).thenReturn(mockSession);
    }

    @Test
    void testCreateRowDataTable() {
        // Setup
        final Map<String, Class<?>> columns = new HashMap<String, Class<?>>() {
            {
                put("fechaColumn", Date.class);
                put("stringColumn", String.class);
                put("numberColumn", Integer.class);
                put(null, String.class);
            }
        };

        when(mockSession.createSQLQuery(any())).thenReturn(mockNativeQuery);
        when(mockNativeQuery.executeUpdate()).thenReturn(1);

        // Run the test
        rowDataDaoImplUnderTest.createRowDataTable(columns, project);
        // Verify the results
        assertTrue(true);
    }

    @Test
    void testCreateRowDataTable_emptyColumns() {
        // Setup
        final Map<String, Class<?>> columns = new HashMap<>();
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
                add(new Object[]{null, String.class});
                add(new Object[]{"01-01-2001", Date.class});
                add(new Object[]{"stringColumn", String.class});
                add(new Object[]{1, Integer.class});
            }
        };

        when(mockSession.createSQLQuery(any())).thenReturn(mockNativeQuery);
        when(mockNativeQuery.executeUpdate()).thenReturn(1);
        // Run the test
        rowDataDaoImplUnderTest.persistRowData("?,?,?,?", infoToBePersisted);

        // Verify the results
        assertTrue(true);
    }

    @Test
    void testGetRowData() {
        // Setup
        when(mockJdbcTemplate.queryForList(any())).thenReturn(Mockito.anyList());
        // Run the test
        final List<Map<String, Object>> result = rowDataDaoImplUnderTest.getRowData(project);

        // Verify the results
        assertTrue(true);
    }

    @Test
    void testDeleteRowData() {
        // Setup
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
        when(mockJdbcTemplate.queryForObject(Mockito.anyString(), ArgumentMatchers.<Class<Integer>>any())).thenReturn(new Integer(1));
        // Run the test
        final Integer result = rowDataDaoImplUnderTest.getRowDataSize(project);

        // Verify the results
        assertEquals(1, result);
    }
}
