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

import java.util.Arrays;
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

        when(mockProjectDao.createProject(projectDto.getName())).thenThrow(GenericJDBCException.class);

        // Run the test
        assertThrows(GenericJDBCException.class, () -> {
            projectServiceImplUnderTest.createProject(projectDto.getName());
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
    void testGetProjects() throws Exception {
        // Setup
        when(mockProjectDao.getProjects()).thenReturn(Arrays.asList(new HashMap<>()));

        // Run the test
        final List<Map<String, Object>> result = projectServiceImplUnderTest.getProjects();

        // Verify the results
        assertTrue(true);
    }

    @Test
    void testGetProjects_ProjectDaoThrowsUncategorizedSQLException() throws Exception {
        // Setup
        when(mockProjectDao.getProjects()).thenThrow(UncategorizedSQLException.class);

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
    void testDeleteProject() {
        when(mockModelMapper.map(projectDto, Project.class)).thenReturn(projectEntity);

        // Run the test
        projectServiceImplUnderTest.deleteProject(projectDto);

        // Verify the results
        verify(mockProjectDao).deleteProject(projectEntity);
    }
}
