package com.buildmaster.projecttracker.service;

import com.buildmaster.projecttracker.dto.user.UserResponseDto;
import com.buildmaster.projecttracker.dto.user.UserRoleUpdateDto;
import com.buildmaster.projecttracker.dto.user.UserUpdateDto;
import com.buildmaster.projecttracker.model.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface UserService {
    
    /**
     * Get current authenticated user
     */
    UserResponseDto getCurrentUser();
    
    /**
     * Get user by ID
     */
    UserResponseDto getUserById(Long userId);
    
    /**
     * Get all users with pagination
     */
    Page<UserResponseDto> getAllUsers(Pageable pageable);
    
    /**
     * Search users by term
     */
    Page<UserResponseDto> searchUsers(String searchTerm, Pageable pageable);
    
    /**
     * Update user profile
     */
    UserResponseDto updateUserProfile(Long userId, UserUpdateDto updateDto, String actorName);
    
    /**
     * Update user role (Admin only)
     */
    UserResponseDto updateUserRole(Long userId, UserRoleUpdateDto roleUpdateDto, String actorName);
    
    /**
     * Enable/disable user (Admin only)
     */
    UserResponseDto toggleUserStatus(Long userId, boolean enabled, String actorName);
    
    /**
     * Delete user (Admin only)
     */
    void deleteUser(Long userId, String actorName);
    
    /**
     * Get users by role
     */
    List<UserResponseDto> getUsersByRole(String roleName);
    
    /**
     * Get user statistics
     */
    Map<String, Object> getUserStatistics();
    
    /**
     * Check if current user can access resource
     */
    boolean canAccessUser(Long userId);
    
    /**
     * Get current user entity
     */
    User getCurrentUserEntity();
}