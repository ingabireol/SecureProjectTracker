package com.buildmaster.projecttracker.repository;

import com.buildmaster.projecttracker.model.user.AuthProvider;
import com.buildmaster.projecttracker.model.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    // Find user by username
    Optional<User> findByUsername(String username);
    
    // Find user by email
    Optional<User> findByEmail(String email);
    
    // Find user by username or email (for login)
    @Query("SELECT u FROM User u WHERE u.username = :usernameOrEmail OR u.email = :usernameOrEmail")
    Optional<User> findByUsernameOrEmail(@Param("usernameOrEmail") String usernameOrEmail);
    
    // Check if username exists
    boolean existsByUsername(String username);
    
    // Check if email exists
    boolean existsByEmail(String email);
    
    // Find users by provider
    List<User> findByProvider(AuthProvider provider);
    
    // Find user by provider and provider ID (for OAuth2)
    Optional<User> findByProviderAndProviderId(AuthProvider provider, String providerId);
    
    // Find users by role name
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = :roleName")
    List<User> findByRoleName(@Param("roleName") String roleName);
    
    // Find users with pagination by role
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = :roleName")
    Page<User> findByRoleName(@Param("roleName") String roleName, Pageable pageable);
    
    // Find enabled users
    List<User> findByEnabledTrue();
    
    // Find disabled users
    List<User> findByEnabledFalse();
    
    // Search users by name or email
    @Query("SELECT u FROM User u WHERE " +
           "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.username) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<User> searchUsers(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    // Find users created between dates
    List<User> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    // Find users who logged in after a certain date
    List<User> findByLastLoginAfter(LocalDateTime date);
    
    // Find users who never logged in
    List<User> findByLastLoginIsNull();
    
    // Update last login timestamp
    @Modifying
    @Query("UPDATE User u SET u.lastLogin = :loginTime WHERE u.id = :userId")
    int updateLastLogin(@Param("userId") Long userId, @Param("loginTime") LocalDateTime loginTime);
    
    // Enable/disable user
    @Modifying
    @Query("UPDATE User u SET u.enabled = :enabled WHERE u.id = :userId")
    int updateUserStatus(@Param("userId") Long userId, @Param("enabled") boolean enabled);
    
    // Count users by provider
    long countByProvider(AuthProvider provider);
    
    // Count users by role
    @Query("SELECT COUNT(u) FROM User u JOIN u.roles r WHERE r.name = :roleName")
    long countByRoleName(@Param("roleName") String roleName);
    
    // Get user statistics
    @Query("SELECT u.provider, COUNT(u) FROM User u GROUP BY u.provider")
    List<Object[]> getUserProviderStatistics();
    
    // Find users without any roles
    @Query("SELECT u FROM User u WHERE u.roles IS EMPTY")
    List<User> findUsersWithoutRoles();
}