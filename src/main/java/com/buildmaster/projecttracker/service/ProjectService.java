package com.buildmaster.projecttracker.service;

import com.buildmaster.projecttracker.dto.project.ProjectDetailResponseDto;
import com.buildmaster.projecttracker.dto.project.ProjectRequestDto;
import com.buildmaster.projecttracker.dto.project.ProjectResponseDto;
import com.buildmaster.projecttracker.model.ProjectStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface ProjectService {
    
    /**
     * Create a new project
     */
    ProjectResponseDto createProject(ProjectRequestDto projectRequestDto, String actorName);
    
    /**
     * Update an existing project
     */
    ProjectResponseDto updateProject(Long projectId, ProjectRequestDto projectRequestDto, String actorName);
    
    /**
     * Delete a project and all its tasks
     */
    void deleteProject(Long projectId, String actorName);
    
    /**
     * Get project by ID
     */
    ProjectResponseDto getProjectById(Long projectId);
    
    /**
     * Get detailed project information including tasks
     */
    ProjectDetailResponseDto getProjectDetails(Long projectId);
    
    /**
     * Get all projects with pagination and sorting
     */
    Page<ProjectResponseDto> getAllProjects(Pageable pageable);
    
    /**
     * Get projects by status with pagination
     */
    Page<ProjectResponseDto> getProjectsByStatus(ProjectStatus status, Pageable pageable);
    
    /**
     * Search projects by name with pagination
     */
    Page<ProjectResponseDto> searchProjectsByName(String name, Pageable pageable);
    
    /**
     * Get projects without any tasks
     */
    List<ProjectResponseDto> getProjectsWithoutTasks();
    
    /**
     * Get overdue projects
     */
    List<ProjectResponseDto> getOverdueProjects();
    
    /**
     * Get projects with deadline between dates
     */
    List<ProjectResponseDto> getProjectsByDeadlineRange(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Get project status statistics
     */
    Map<String, Long> getProjectStatusStatistics();
    
    /**
     * Check if project exists by name
     */
    boolean existsByName(String name);
    
    /**
     * Get projects created within date range
     */
    List<ProjectResponseDto> getProjectsByCreationDateRange(LocalDateTime startDate, LocalDateTime endDate);
}