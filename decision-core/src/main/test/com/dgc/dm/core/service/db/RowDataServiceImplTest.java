
package com.dgc.dm.core.service.db;

class RowDataServiceImplTest {
//    @Mock
//    private ModelMapper mockModelMapper;
//    @Mock
//    private RowDataDao mockRowDataDao;
//    @InjectMocks
//    private RowDataServiceImpl rowDataServiceImplUnderTest;
//
//    final ProjectDto project = new ProjectDto(0, "name", "rowDataTableName", "emailTemplate", "content".getBytes());
//    final Project projectEntity = new Project(0, "name", "rowDataTableName", "emailTemplate", "content".getBytes());
//
//    @BeforeEach
//    void setUp() {
//        initMocks(this);
//    }
//
//    @Test
//    void testCreateRowDataTable() {
//        // Setup
//        final Map<String, Class<?>> columns = new HashMap<>();
//
//        when(mockModelMapper.map(project, Project.class)).thenReturn(projectEntity);
//        doNothing().when(mockRowDataDao).createRowDataTable(new HashMap<>(), projectEntity);
//        // Run the test
//        rowDataServiceImplUnderTest.createRowDataTable(columns, project);
//
//        // Verify the results
//        verify(mockRowDataDao).createRowDataTable(new HashMap<>(), projectEntity);
//    }
//
//    @Test
//    void testPersistRowData() {
//        // Setup
//        final List<Object[]> infoToBePersisted = new ArrayList<Object[]>() {
//            {
//                add(new Object[]{"value"});
//            }
//        };
//        doNothing().when(mockRowDataDao).persistRowData("insertSentence", infoToBePersisted);
//
//        // Run the test
//        rowDataServiceImplUnderTest.persistRowData("insertSentence", infoToBePersisted);
//
//        // Verify the results
//        verify(mockRowDataDao).persistRowData("insertSentence", infoToBePersisted);
//    }
//
//    @Test
//    void testGetRowData() {
//        // Setup
//        when(mockModelMapper.map(project, Project.class)).thenReturn(projectEntity);
//        when(mockRowDataDao.getRowData(projectEntity)).thenReturn(Arrays.asList(new HashMap<>()));
//
//        // Run the test
//        final List<Map<String, Object>> result = rowDataServiceImplUnderTest.getRowData(project);
//
//        // Verify the results
//        verify(mockRowDataDao).getRowData(projectEntity);
//    }
//
//    @Test
//    void testDeleteRowData() {
//        // Setup
//        when(mockModelMapper.map(project, Project.class)).thenReturn(projectEntity);
//        doNothing().when(mockRowDataDao).deleteRowData(projectEntity);
//        // Run the test
//        rowDataServiceImplUnderTest.deleteRowData(project);
//        // Verify the results
//        verify(mockRowDataDao).deleteRowData(projectEntity);
//    }
//
//    @Test
//    void testGetRowDataSize() {
//        // Setup
//        when(mockRowDataDao.getRowDataSize(projectEntity)).thenReturn(0);
//        when(mockModelMapper.map(project, Project.class)).thenReturn(projectEntity);
//
//        // Run the test
//        final Integer result = rowDataServiceImplUnderTest.getRowDataSize(project);
//
//        // Verify the results
//        assertEquals(0, result);
//    }
}
