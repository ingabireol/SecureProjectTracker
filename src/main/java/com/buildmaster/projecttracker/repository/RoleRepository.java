package com.buildmaster.projecttracker.repository;

import com.buildmaster.projecttracker.model.role.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    
    // Find role by name
    Optional<Role> findByName(String name);
    
    // Check if role exists by name
    boolean existsByName(String name);
    
    // Find roles by name containing (case insensitive)
    List<Role> findByNameContainingIgnoreCase(String name);
    
    // Find all roles ordered by name
    List<Role> findAllByOrderByNameAsc();
    
    // Get role statistics with user counts
    @Query("SELECT r.name, COUNT(u) FROM Role r LEFT JOIN r.users u GROUP BY r.id, r.name ORDER BY r.name")
    List<Object[]> getRoleStatistics();
    
    // Find roles that have users
    @Query("SELECT DISTINCT r FROM Role r WHERE r.users IS NOT EMPTY")
    List<Role> findRolesWithUsers();
    
    // Find roles that have no users
    @Query("SELECT r FROM Role r WHERE r.users IS EMPTY")
    List<Role> findRolesWithoutUsers();
    
    // Count users for a specific role
    @Query("SELECT COUNT(u) FROM Role r JOIN r.users u WHERE r.name = :roleName")
    long countUsersByRoleName(@Param("roleName") String roleName);
}