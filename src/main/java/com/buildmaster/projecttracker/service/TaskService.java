package com.buildmaster.projecttracker.service;

import com.buildmaster.projecttracker.dto.task.BulkTaskUpdateDto;
import com.buildmaster.projecttracker.dto.task.TaskRequestDto;
import com.buildmaster.projecttracker.dto.task.TaskResponseDto;
import com.buildmaster.projecttracker.model.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface TaskService {
    
    /**
     * Create a new task
     */
    TaskResponseDto createTask(TaskRequestDto taskRequestDto, String actorName);
    
    /**
     * Update an existing task
     */
    TaskResponseDto updateTask(Long taskId, TaskRequestDto taskRequestDto, String actorName);
    
    /**
     * Delete a task
     */
    void deleteTask(Long taskId, String actorName);
    
    /**
     * Get task by ID
     */
    TaskResponseDto getTaskById(Long taskId);
    
    /**
     * Get all tasks with pagination and sorting
     */
    Page<TaskResponseDto> getAllTasks(Pageable pageable);
    
    /**
     * Get tasks by project with pagination
     */
    Page<TaskResponseDto> getTasksByProject(Long projectId, Pageable pageable);
    
    /**
     * Get tasks by assigned developer with pagination
     */
    Page<TaskResponseDto> getTasksByDeveloper(Long developerId, Pageable pageable);
    
    /**
     * Get tasks by status with pagination
     */
    Page<TaskResponseDto> getTasksByStatus(TaskStatus status, Pageable pageable);
    
    /**
     * Get unassigned tasks with pagination
     */
    Page<TaskResponseDto> getUnassignedTasks(Pageable pageable);
    
    /**
     * Search tasks by title with pagination
     */
    Page<TaskResponseDto> searchTasksByTitle(String title, Pageable pageable);
    
    /**
     * Get overdue tasks
     */
    List<TaskResponseDto> getOverdueTasks();
    
    /**
     * Get tasks due within specified days
     */
    List<TaskResponseDto> getTasksDueWithin(int days);
    
    /**
     * Get tasks with no due date
     */
    List<TaskResponseDto> getTasksWithoutDueDate();
    
    /**
     * Assign task to developer
     */
    TaskResponseDto assignTask(Long taskId, Long developerId, String actorName);
    
    /**
     * Unassign task from developer
     */
    TaskResponseDto unassignTask(Long taskId, String actorName);
    
    /**
     * Bulk assign tasks to developer
     */
    int bulkAssignTasks(List<Long> taskIds, Long developerId, String actorName);
    
    /**
     * Bulk update task status for a project
     */
    int bulkUpdateTaskStatusByProject(Long projectId, TaskStatus status, String actorName);
    
    /**
     * Bulk update tasks
     */
    List<TaskResponseDto> bulkUpdateTasks(List<Long> taskIds, BulkTaskUpdateDto updateDto, String actorName);
    
    /**
     * Get tasks by multiple criteria with pagination
     */
    Page<TaskResponseDto> getTasksByCriteria(Long projectId, Long developerId, 
                                           TaskStatus status, String title, Pageable pageable);
    
    /**
     * Get task status statistics for a project
     */
    Map<String, Long> getTaskStatusStatisticsByProject(Long projectId);
    
    /**
     * Get task status statistics for a developer
     */
    Map<String, Long> getTaskStatusStatisticsByDeveloper(Long developerId);
    
    /**
     * Get overall task statistics
     */
    Map<String, Long> getOverallTaskStatistics();
    
    /**
     * Get tasks created within date range
     */
    List<TaskResponseDto> getTasksByCreationDateRange(LocalDateTime startDate, LocalDateTime endDate);
}