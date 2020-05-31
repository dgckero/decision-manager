package com.dgc.dm.core.service.db;

import com.dgc.dm.core.db.dao.ProjectDao;
import com.dgc.dm.core.db.model.Project;
import com.dgc.dm.core.dto.ProjectDto;
import org.hibernate.exception.GenericJDBCException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.modelmapper.ModelMapper;
import org.springframework.jdbc.UncategorizedSQLException;
import org.sqlite.SQLiteErrorCode;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

class ProjectServiceImplTest {
    @Mock
    private ModelMapper mockModelMapper;

    @Mock
    private ProjectDao mockProjectDao;

    @InjectMocks
    private ProjectServiceImpl projectServiceImplUnderTest;

    final ProjectDto projectDto = new ProjectDto(0, "name", "rowDataTableName", "emailTemplate", "content".getBytes());
    final Project projectEntity = new Project(0, "name", "rowDataTableName", "emailTemplate", "content".getBytes());

    @BeforeEach
    void setUp() {
        initMocks(this);
    }

    @Test
    void testCreateProject() {
        // Setup
        when(mockModelMapper.map(projectEntity, ProjectDto.class)).thenReturn(projectDto);

        when(mockProjectDao.createProject(projectEntity.getName())).thenReturn(projectEntity);

        // Run the test
        final ProjectDto result = projectServiceImplUnderTest.createProject(projectEntity.getName());

        // Verify the results
        assertEquals(projectDto, result);
    }

    @Test
    void testCreateProject_ThrowsGenericJDBCException() {
        // Setup
        String projectName = projectDto.getName();
        when(mockProjectDao.createProject(projectName)).thenThrow(GenericJDBCException.class);

        // Run the test
        assertThrows(GenericJDBCException.class, () -> {
            projectServiceImplUnderTest.createProject(projectName);
        });
    }

    @Test
    void testUpdateProject() {
        // Setup
        when(mockModelMapper.map(projectDto, Project.class)).thenReturn(projectEntity);
        doNothing().when(mockProjectDao).updateProject(projectEntity);

        // Run the test
        projectServiceImplUnderTest.updateProject(projectDto);

        // Verify the results
        verify(mockProjectDao).updateProject(projectEntity);
    }

    @Test
    void testGetProjects() {
        // Setup
        List<Map<String, Object>> projects = new ArrayList<Map<String, Object>>() {
            {
                add(
                        new HashMap<String, Object>() {
                            {
                                put("projectA", projectDto);
                            }
                        }
                );
            }
        };
        when(mockProjectDao.getProjects()).thenReturn(projects);

        // Run the test
        final List<Map<String, Object>> result = projectServiceImplUnderTest.getProjects();

        // Verify the results
        assertNotNull(result);
    }

    @Test
    void testGetProjects_notFound() {
        // Setup
        when(mockProjectDao.getProjects()).thenReturn(new ArrayList<>());

        // Run the test
        final List<Map<String, Object>> result = projectServiceImplUnderTest.getProjects();

        // Verify the results
        assertNull(result);
    }

    @Test
    void testGetProjects_ProjectDaoThrowsUncategorizedSQLException_SQLITE_ERROR() {
        // Setup
        UncategorizedSQLException ex = new UncategorizedSQLException(null, null, new SQLException("Error", "Error", SQLiteErrorCode.SQLITE_ERROR.code));
        when(mockProjectDao.getProjects()).thenThrow(ex);

        // Run the test
        final List<Map<String, Object>> result = projectServiceImplUnderTest.getProjects();

        // Verify the results
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetProjects_ProjectDaoThrowsUncategorizedSQLException_NO_SQLITE_ERROR() {
        // Setup
        UncategorizedSQLException ex = new UncategorizedSQLException(null, null, new SQLException("Error", "Error", SQLiteErrorCode.SQLITE_LOCKED.code));
        when(mockProjectDao.getProjects()).thenThrow(ex);

        assertThrows(UncategorizedSQLException.class, () -> {
            projectServiceImplUnderTest.getProjects();
        });
    }

    @Test
    void testGetProject() {
        // Setup
        when(mockProjectDao.getProject(0)).thenReturn(projectEntity);
        when(mockModelMapper.map(projectEntity, ProjectDto.class)).thenReturn(projectDto);

        // Run the test
        final ProjectDto result = projectServiceImplUnderTest.getProject(0);

        // Verify the results
        assertEquals(projectDto, result);
    }

    @Test
    void testGetProject_noProjectFound() {
        // Setup
        when(mockProjectDao.getProject(0)).thenReturn(null);
        when(mockModelMapper.map(projectEntity, ProjectDto.class)).thenReturn(projectDto);

        // Run the test
        final ProjectDto result = projectServiceImplUnderTest.getProject(0);

        // Verify the results
        assertNull(result);
    }

    @Test
    void testDeleteProject() {
        when(mockModelMapper.map(projectDto, Project.class)).thenReturn(projectEntity);

        // Run the test
        projectServiceImplUnderTest.deleteProject(projectDto);

        // Verify the results
        verify(mockProjectDao).deleteProject(projectEntity);
    }
}
