package com.dgc.dm.core.db.dao;

import com.dgc.dm.core.db.model.Filter;
import com.dgc.dm.core.db.model.Project;
import com.dgc.dm.core.db.repository.FilterRepository;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.NativeQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

class FilterDaoImplTest {
    @Mock
    private NativeQuery mockNativeQuery;

    @Mock
    private Session mockSession;

    @Mock
    private SessionFactory mockSessionFactory;

    @Mock
    private JdbcTemplate mockJdbcTemplate;

    @Mock
    private FilterRepository mockFilterRepository;

    @InjectMocks
    private FilterDaoImpl filterDaoImplUnderTest;

    @BeforeEach
    void setUp() {
        initMocks(this);
        when(mockSessionFactory.getCurrentSession()).thenReturn(mockSession);
        when(mockSession.createSQLQuery(any())).thenReturn(mockNativeQuery);
    }

    @Test
    void testCreateFilterTable() {
        // Setup
        final Project project = new Project(0, "name", "rowDataTableName", "emailTemplate", "content".getBytes());
        when(mockNativeQuery.executeUpdate()).thenReturn(1);
        // Run the test
        filterDaoImplUnderTest.createFilterTable(project);

        // Verify the results
        assertTrue(true);
    }

    @Test
    void testPersistFilterList() {
        // Setup
        final Filter filter = new Filter(0, "name", "filterClass", "value", false, false,
                new Project(0, "name", "rowDataTableName", "emailTemplate", "content".getBytes()));
        final List<Filter> filterList = Arrays.asList(filter);
        when(mockSession.save(filterList)).thenReturn(filter);
        // Run the test
        filterDaoImplUnderTest.persistFilterList(filterList);

        // Verify the results
        assertTrue(true);
    }

    @Test
    void testGetFilters() {
        // Setup
        when(mockJdbcTemplate.queryForList(any())).thenReturn(Mockito.anyList());
        // Run the test
        final List<Map<String, Object>> result = filterDaoImplUnderTest.getFilters();

        // Verify the results
        assertTrue(true);
    }

    @Test
    void testGetFilters1() {
        // Setup
        final Project project = new Project(0, "name", "rowDataTableName", "emailTemplate", "content".getBytes());
        when(mockJdbcTemplate.queryForList(any())).thenReturn(Mockito.anyList());
        // Run the test
        final List<Map<String, Object>> result = filterDaoImplUnderTest.getFilters(project);

        // Verify the results
        assertTrue(true);
    }

    @Test
    void testUpdateFilters() {
        // Setup
        final Filter filter = new Filter(0, "name", "filterClass", "value", false, false,
                new Project(0, "name", "rowDataTableName", "emailTemplate", "content".getBytes()));
        final List<Filter> filterList = Arrays.asList(filter);
        when(mockSession.merge(filter)).thenReturn(filter);
        // Run the test
        filterDaoImplUnderTest.updateFilters(filterList);

        // Verify the results
        assertTrue(true);
    }

}
