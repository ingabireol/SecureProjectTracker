package com.buildmaster.projecttracker.repository;

import com.buildmaster.projecttracker.model.Project;
import com.buildmaster.projecttracker.model.ProjectStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    // Find projects by status
    Page<Project> findByStatus(ProjectStatus status, Pageable pageable);
    
    // Find projects by status (list version)
    List<Project> findByStatus(ProjectStatus status);
    
    // Find projects with deadline before a certain date
    List<Project> findByDeadlineBefore(LocalDateTime deadline);
    
    // Find projects with deadline between dates
    List<Project> findByDeadlineBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    // Find projects by name containing (case insensitive)
    Page<Project> findByNameContainingIgnoreCase(String name, Pageable pageable);
    
    // Custom query to find projects without any tasks
    @Query("SELECT p FROM Project p WHERE p.tasks IS EMPTY")
    List<Project> findProjectsWithoutTasks();
    
    // Custom query to find projects with task count
    @Query("SELECT p FROM Project p LEFT JOIN p.tasks t GROUP BY p.id HAVING COUNT(t) = :taskCount")
    List<Project> findProjectsWithTaskCount(@Param("taskCount") long taskCount);
    
    // Find overdue projects (deadline passed and status not completed)
    @Query("SELECT p FROM Project p WHERE p.deadline < :currentDate AND p.status != 'COMPLETED'")
    List<Project> findOverdueProjects(@Param("currentDate") LocalDateTime currentDate);
    
    // Find projects by status with pagination and sorting
    @Query("SELECT p FROM Project p WHERE p.status = :status ORDER BY p.deadline ASC")
    Page<Project> findByStatusOrderByDeadline(@Param("status") ProjectStatus status, Pageable pageable);
    
    // Find projects created between dates
    List<Project> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    // Check if project exists by name (case insensitive)
    boolean existsByNameIgnoreCase(String name);
    
    // Find project by name (case insensitive)
    Optional<Project> findByNameIgnoreCase(String name);
    
    // Count projects by status
    long countByStatus(ProjectStatus status);
    
    // Native query example - get project statistics
    @Query(value = "SELECT p.status, COUNT(*) as count FROM projects p GROUP BY p.status", nativeQuery = true)
    List<Object[]> getProjectStatusStatistics();
}