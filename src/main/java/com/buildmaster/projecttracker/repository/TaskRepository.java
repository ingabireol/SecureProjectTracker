package com.buildmaster.projecttracker.repository;

import com.buildmaster.projecttracker.model.Task;
import com.buildmaster.projecttracker.model.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    
    // Find tasks by project ID
    Page<Task> findByProjectId(Long projectId, Pageable pageable);
    List<Task> findByProjectId(Long projectId);
    
    // Find tasks by assigned developer ID
    Page<Task> findByAssignedDeveloperId(Long developerId, Pageable pageable);
    List<Task> findByAssignedDeveloperId(Long developerId);
    
    // Find tasks by status
    Page<Task> findByStatus(TaskStatus status, Pageable pageable);
    List<Task> findByStatus(TaskStatus status);
    
    // Find unassigned tasks
    Page<Task> findByAssignedDeveloperIsNull(Pageable pageable);
    List<Task> findByAssignedDeveloperIsNull();
    
    // Find overdue tasks
    @Query("SELECT t FROM Task t WHERE t.dueDate < :currentDate AND t.status != 'COMPLETED'")
    List<Task> findOverdueTasks(@Param("currentDate") LocalDateTime currentDate);
    
    // Find tasks due within specified days
    @Query("SELECT t FROM Task t WHERE t.dueDate BETWEEN :startDate AND :endDate AND t.status != 'COMPLETED'")
    List<Task> findTasksDueWithin(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    // Find tasks by project and status
    List<Task> findByProjectIdAndStatus(Long projectId, TaskStatus status);
    
    // Find tasks by developer and status
    List<Task> findByAssignedDeveloperIdAndStatus(Long developerId, TaskStatus status);
    
    // Find tasks by title containing (case insensitive)
    Page<Task> findByTitleContainingIgnoreCase(String title, Pageable pageable);
    
    // Count tasks by status
    long countByStatus(TaskStatus status);
    
    // Count tasks by project
    long countByProjectId(Long projectId);
    
    // Count tasks by developer
    long countByAssignedDeveloperId(Long developerId);
    
    // Find tasks created between dates
    List<Task> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    // Find tasks with no due date
    List<Task> findByDueDateIsNull();
    
    // Bulk update task status by project
    @Modifying
    @Query("UPDATE Task t SET t.status = :status WHERE t.project.id = :projectId")
    int updateTaskStatusByProject(@Param("projectId") Long projectId, @Param("status") TaskStatus status);
    
    // Bulk assign tasks to developer
    @Modifying
    @Query("UPDATE Task t SET t.assignedDeveloper.id = :developerId WHERE t.id IN :taskIds")
    int bulkAssignTasks(@Param("taskIds") List<Long> taskIds, @Param("developerId") Long developerId);
    
    // Get task status statistics for a project
    @Query("SELECT t.status, COUNT(t) FROM Task t WHERE t.project.id = :projectId GROUP BY t.status")
    List<Object[]> getTaskStatusStatisticsByProject(@Param("projectId") Long projectId);
    
    // Get task statistics by developer
    @Query("SELECT t.status, COUNT(t) FROM Task t WHERE t.assignedDeveloper.id = :developerId GROUP BY t.status")
    List<Object[]> getTaskStatusStatisticsByDeveloper(@Param("developerId") Long developerId);
    
    // Find tasks by multiple criteria
    @Query("SELECT t FROM Task t WHERE " +
           "(:projectId IS NULL OR t.project.id = :projectId) AND " +
           "(:developerId IS NULL OR t.assignedDeveloper.id = :developerId) AND " +
           "(:status IS NULL OR t.status = :status) AND " +
           "(:title IS NULL OR LOWER(t.title) LIKE LOWER(CONCAT('%', :title, '%')))")
    Page<Task> findTasksByCriteria(@Param("projectId") Long projectId,
                                   @Param("developerId") Long developerId,
                                   @Param("status") TaskStatus status,
                                   @Param("title") String title,
                                   Pageable pageable);
}