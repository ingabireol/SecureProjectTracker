package com.buildmaster.projecttracker.repository;

import com.buildmaster.projecttracker.model.Developer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeveloperRepository extends JpaRepository<Developer, Long> {
    
    // Find developer by email (unique)
    Optional<Developer> findByEmail(String email);
    
    // Check if developer exists by email
    boolean existsByEmail(String email);
    
    // Find developers by name containing (case insensitive)
    Page<Developer> findByNameContainingIgnoreCase(String name, Pageable pageable);
    
    // Find developers by skill
    @Query("SELECT d FROM Developer d JOIN d.skills s WHERE s = :skill")
    List<Developer> findBySkill(@Param("skill") String skill);
    
    // Find developers with specific skills (contains any of the skills)
    @Query("SELECT DISTINCT d FROM Developer d JOIN d.skills s WHERE s IN :skills")
    List<Developer> findBySkillsIn(@Param("skills") List<String> skills);
    
    // Find top developers by task count
    @Query("SELECT d FROM Developer d LEFT JOIN d.assignedTasks t GROUP BY d.id ORDER BY COUNT(t) DESC")
    List<Developer> findTopDevelopersByTaskCount(Pageable pageable);
    
    // Find developers with no assigned tasks
    @Query("SELECT d FROM Developer d WHERE d.assignedTasks IS EMPTY")
    List<Developer> findDevelopersWithoutTasks();
    
    // Find developers with task count greater than specified
    @Query("SELECT d FROM Developer d LEFT JOIN d.assignedTasks t GROUP BY d.id HAVING COUNT(t) > :minTasks")
    List<Developer> findDevelopersWithTaskCountGreaterThan(@Param("minTasks") long minTasks);
    
    // Find developers with task count equal to specified number
    @Query("SELECT d FROM Developer d LEFT JOIN d.assignedTasks t GROUP BY d.id HAVING COUNT(t) = :taskCount")
    List<Developer> findDevelopersWithTaskCount(@Param("taskCount") long taskCount);
    
    // Find developers by name or email containing search term
    @Query("SELECT d FROM Developer d WHERE LOWER(d.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(d.email) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Developer> findByNameOrEmailContaining(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    // Count developers by skill
    @Query("SELECT COUNT(DISTINCT d) FROM Developer d JOIN d.skills s WHERE s = :skill")
    long countBySkill(@Param("skill") String skill);
    
    // Get developer task statistics
    @Query(value = "SELECT d.id, d.name, d.email, COUNT(t.id) as task_count FROM developers d LEFT JOIN tasks t ON d.id = t.assigned_developer_id GROUP BY d.id, d.name, d.email ORDER BY task_count DESC", nativeQuery = true)
    List<Object[]> getDeveloperTaskStatistics();
    
    // Find developers available for assignment (with less than specified task count)
    @Query("SELECT d FROM Developer d LEFT JOIN d.assignedTasks t GROUP BY d.id HAVING COUNT(t) < :maxTasks")
    List<Developer> findAvailableDevelopers(@Param("maxTasks") long maxTasks);
}