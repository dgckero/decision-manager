package com.dgc.dm.core.db.dao;

import com.dgc.dm.core.db.model.Project;
import com.dgc.dm.core.db.repository.ProjectRepository;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.NativeQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.jdbc.UncategorizedSQLException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

class ProjectDaoImplTest {
    @Mock
    private NativeQuery mockNativeQuery;

    @Mock
    private Session mockSession;

    @Mock
    private SessionFactory mockSessionFactory;

    @Mock
    private JdbcTemplate mockJdbcTemplate;

    @Mock
    private ProjectRepository mockProjectRepository;

    @InjectMocks
    private ProjectDaoImpl projectDaoImplUnderTest;

    @BeforeEach
    void setUp() {
        initMocks(this);
        when(mockSessionFactory.getCurrentSession()).thenReturn(mockSession);
        when(mockSession.createSQLQuery(any())).thenReturn(mockNativeQuery);
    }

    @Test
    void testCreateProjectTable() {
        // Setup
        when(mockNativeQuery.executeUpdate()).thenReturn(1);
        // Run the test
        projectDaoImplUnderTest.createProjectTable();

        // Verify the results
        verify(mockNativeQuery).executeUpdate();
    }

    @Test
    void testCreateProject() {
        // Setup
        doNothing().when(mockSession).persist(any(Project.class));
        // Run the test
        final Project result = projectDaoImplUnderTest.createProject("projectName");

        // Verify the results
        verify(mockSession).persist(any(Project.class));
    }

    @Test
    void testUpdateProject() {
        // Setup
        final Project project = new Project(0, "name", "rowDataTableName", "emailTemplate", "content".getBytes());
        when(mockSession.merge(project)).thenReturn(project);
        // Run the test
        projectDaoImplUnderTest.updateProject(project);

        // Verify the results
        verify(mockSession).merge(project);
    }

    @Test
    void testGetProjects() {
        // Setup
        when(mockJdbcTemplate.queryForList(any())).thenReturn(Mockito.anyList());
        // Run the test
        final List<Map<String, Object>> result = projectDaoImplUnderTest.getProjects();

        // Verify the results
        verify(mockJdbcTemplate).queryForList(any());
    }

    @Test
    void testGetProjects_ThrowsUncategorizedSQLException() {
        // Setup
        when(mockJdbcTemplate.queryForList(any())).thenThrow(UncategorizedSQLException.class);
        // Run the test
        assertThrows(UncategorizedSQLException.class, () -> {
            projectDaoImplUnderTest.getProjects();
        });
    }

    @Test
    void testGetProject() {
        // Setup
        final Project project = new Project(0, "name", "rowDataTableName", "emailTemplate", "content".getBytes());
        when(mockSession.find(Project.class, project.getId())).thenReturn(project);
        // Run the test
        final Project result = projectDaoImplUnderTest.getProject(0);

        // Verify the results
        verify(mockSession).find(Project.class, project.getId());
    }

    @Test
    void testDeleteProject() {
        // Setup
        final Project project = new Project(0, "name", "rowDataTableName", "emailTemplate", "content".getBytes());
        doNothing().when(mockSession).delete(project);
        // Run the test
        projectDaoImplUnderTest.deleteProject(project);

        // Verify the results
        verify(mockSession).delete(project);
    }
}
