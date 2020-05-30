package com.dgc.dm.core.db.dao;

class FilterDaoImplTest {
//    @Mock
//    private NativeQuery mockNativeQuery;
//
//    @Mock
//    private Session mockSession;
//
//    @Mock
//    private SessionFactory mockSessionFactory;
//
//    @Mock
//    private JdbcTemplate mockJdbcTemplate;
//
//    @InjectMocks
//    private FilterDaoImpl filterDaoImplUnderTest;
//
//    @BeforeEach
//    void setUp() {
//        initMocks(this);
//        when(mockSessionFactory.getCurrentSession()).thenReturn(mockSession);
//        when(mockSession.createSQLQuery(any())).thenReturn(mockNativeQuery);
//    }
//
//    @Test
//    void testCreateFilterTable() {
//        // Setup
//        final Project project = new Project(0, "name", "rowDataTableName", "emailTemplate", "content".getBytes());
//        when(mockNativeQuery.executeUpdate()).thenReturn(1);
//        // Run the test
//        filterDaoImplUnderTest.createFilterTable(project);
//
//        // Verify the results
//        assertTrue(true);
//    }
//
//    @Test
//    void testPersistFilterList() {
//        // Setup
//        final Filter filter = new Filter(0, "name", "filterClass", "value", false, false,
//                new Project(0, "name", "rowDataTableName", "emailTemplate", "content".getBytes()));
//        final List<Filter> filterList = Arrays.asList(filter);
//        when(mockSession.save(filterList)).thenReturn(filter);
//        // Run the test
//        filterDaoImplUnderTest.persistFilterList(filterList);
//
//        // Verify the results
//        assertTrue(true);
//    }
//
//    @Test
//    void testGetFilters() {
//        // Setup
//        when(mockJdbcTemplate.queryForList(any())).thenReturn(Mockito.anyList());
//        // Run the test
//        final List<Map<String, Object>> result = filterDaoImplUnderTest.getFilters();
//
//        // Verify the results
//        assertTrue(true);
//    }
//
//    @Test
//    void testGetFilters1() {
//        // Setup
//        final Project project = new Project(0, "name", "rowDataTableName", "emailTemplate", "content".getBytes());
//        when(mockJdbcTemplate.queryForList("Select * from FILTERS where project =:projectId",
//                new MapSqlParameterSource()
//                        .addValue("projectId", project.getId()))).thenReturn(new ArrayList<>());
//        // Run the test
//        final List<Filter> result = filterDaoImplUnderTest.getFilters(project);
//
//        // Verify the results
//        assertNotNull(result);
//    }
//
//    @Test
//    void testUpdateFilters() {
//        // Setup
//        final Filter filter = new Filter(0, "name", "filterClass", "value", false, false,
//                new Project(0, "name", "rowDataTableName", "emailTemplate", "content".getBytes()));
//        final List<Filter> filterList = Arrays.asList(filter);
//        when(mockSession.merge(filter)).thenReturn(filter);
//        // Run the test
//        filterDaoImplUnderTest.updateFilters(filterList);
//
//        // Verify the results
//        assertTrue(true);
//    }
//
//    @Test
//    void getContactFilter() {
//        final Project project = new Project(0, "name", "rowDataTableName", "emailTemplate", "content".getBytes());
//        final Filter filter = new Filter(0, "name", "filterClass", "value", false, false,
//                project);
//
//
//        when(mockJdbcTemplate.queryForObject(any(String.class),
//                any(Object[].class), any(RowMapper.class))).thenReturn(filter);
//
//        Filter result = filterDaoImplUnderTest.getContactFilter(project);
//
//        assertEquals(filter, result);
//    }
}
