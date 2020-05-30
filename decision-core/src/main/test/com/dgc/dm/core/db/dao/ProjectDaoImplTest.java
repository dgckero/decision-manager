package com.dgc.dm.core.db.dao;

class ProjectDaoImplTest {
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
//    private ProjectDaoImpl projectDaoImplUnderTest;
//
//    @BeforeEach
//    void setUp() {
//        initMocks(this);
//        when(mockSessionFactory.getCurrentSession()).thenReturn(mockSession);
//        when(mockSession.createSQLQuery(any())).thenReturn(mockNativeQuery);
//    }
//
//    @Test
//    void testCreateProjectTable() {
//        // Setup
//        when(mockNativeQuery.executeUpdate()).thenReturn(1);
//        // Run the test
//        projectDaoImplUnderTest.createProjectTable();
//
//        // Verify the results
//        verify(mockNativeQuery).executeUpdate();
//    }
//
//    @Test
//    void testCreateProject() {
//        // Setup
//        doNothing().when(mockSession).persist(any(Project.class));
//        // Run the test
//        final Project result = projectDaoImplUnderTest.createProject("projectName");
//
//        // Verify the results
//        verify(mockSession).persist(any(Project.class));
//    }
//
//    @Test
//    void testUpdateProject() {
//        // Setup
//        final Project project = new Project(0, "name", "rowDataTableName", "emailTemplate", "content".getBytes());
//        when(mockSession.merge(project)).thenReturn(project);
//        // Run the test
//        projectDaoImplUnderTest.updateProject(project);
//
//        // Verify the results
//        verify(mockSession).merge(project);
//    }
//
//    @Test
//    void testGetProjects() {
//        // Setup
//        when(mockJdbcTemplate.queryForList(any())).thenReturn(Mockito.anyList());
//        // Run the test
//        final List<Map<String, Object>> result = projectDaoImplUnderTest.getProjects();
//
//        // Verify the results
//        verify(mockJdbcTemplate).queryForList(any());
//    }
//
//    @Test
//    void testGetProjects_ThrowsUncategorizedSQLException() {
//        // Setup
//        when(mockJdbcTemplate.queryForList(any())).thenThrow(UncategorizedSQLException.class);
//        // Run the test
//        assertThrows(UncategorizedSQLException.class, () -> {
//            projectDaoImplUnderTest.getProjects();
//        });
//    }
//
//    @Test
//    void testGetProject() {
//        // Setup
//        final Project project = new Project(0, "name", "rowDataTableName", "emailTemplate", "content".getBytes());
//        when(mockSession.get(Project.class, project.getId())).thenReturn(project);
//        // Run the test
//        final Project result = projectDaoImplUnderTest.getProject(0);
//
//        // Verify the results
//        verify(mockSession).get(Project.class, project.getId());
//    }
//
//    @Test
//    void testDeleteProject() {
//        // Setup
//        final Project project = new Project(0, "name", "rowDataTableName", "emailTemplate", "content".getBytes());
//        doNothing().when(mockSession).delete(project);
//        // Run the test
//        projectDaoImplUnderTest.deleteProject(project);
//
//        // Verify the results
//        verify(mockSession).delete(project);
//    }
}
