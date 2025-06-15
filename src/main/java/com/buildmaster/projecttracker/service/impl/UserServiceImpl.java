package com.buildmaster.projecttracker.service.impl;

import com.buildmaster.projecttracker.dto.user.UserResponseDto;
import com.buildmaster.projecttracker.dto.user.UserRoleUpdateDto;
import com.buildmaster.projecttracker.dto.user.UserUpdateDto;
import com.buildmaster.projecttracker.exception.EntityNotFoundException;
import com.buildmaster.projecttracker.model.AuditAction;
import com.buildmaster.projecttracker.model.EntityType;
import com.buildmaster.projecttracker.model.role.Role;
import com.buildmaster.projecttracker.model.user.User;
import com.buildmaster.projecttracker.repository.RoleRepository;
import com.buildmaster.projecttracker.repository.UserRepository;
import com.buildmaster.projecttracker.service.AuditLogService;
import com.buildmaster.projecttracker.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserServiceImpl implements UserService {
    
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final AuditLogService auditLogService;
    
    @Override
    public UserResponseDto getCurrentUser() {
        User currentUser = getCurrentUserEntity();
        return convertToUserResponseDto(currentUser);
    }
    
    @Override
    public User getCurrentUserEntity() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal().equals("anonymousUser")) {
            throw new IllegalStateException("No authenticated user found");
        }
        
        return (User) authentication.getPrincipal();
    }
    
    @Override
    @PreAuthorize("hasRole('ADMIN') or @userServiceImpl.canAccessUser(#userId)")
    public UserResponseDto getUserById(Long userId) {
        log.debug("Fetching user with ID: {}", userId);
        User user = findUserById(userId);
        return convertToUserResponseDto(user);
    }
    
    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public Page<UserResponseDto> getAllUsers(Pageable pageable) {
        log.debug("Fetching all users with pagination");
        Page<User> users = userRepository.findAll(pageable);
        return users.map(this::convertToUserResponseDto);
    }
    
    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public Page<UserResponseDto> searchUsers(String searchTerm, Pageable pageable) {
        log.debug("Searching users by term: {}", searchTerm);
        Page<User> users = userRepository.searchUsers(searchTerm, pageable);
        return users.map(this::convertToUserResponseDto);
    }
    
    @Override
    @PreAuthorize("hasRole('ADMIN') or @userServiceImpl.canAccessUser(#userId)")
    public UserResponseDto updateUserProfile(Long userId, UserUpdateDto updateDto, String actorName) {
        log.info("Updating user profile for ID: {}", userId);
        
        User user = findUserById(userId);
        
        // Store old data for audit
        Map<String, Object> oldData = createUserAuditPayload(user);
        
        // Update user fields
        if (updateDto.getFirstName() != null) {
            user.setFirstName(updateDto.getFirstName());
        }
        if (updateDto.getLastName() != null) {
            user.setLastName(updateDto.getLastName());
        }
        if (updateDto.getEmail() != null && !updateDto.getEmail().equals(user.getEmail())) {
            // Check email uniqueness
            if (userRepository.existsByEmail(updateDto.getEmail())) {
                throw new IllegalArgumentException("Email is already in use");
            }
            user.setEmail(updateDto.getEmail());
        }
        
        User updatedUser = userRepository.save(user);
        
        // Log the update
        Map<String, Object> payload = new HashMap<>();
        payload.put("oldData", oldData);
        payload.put("newData", createUserAuditPayload(updatedUser));
        
        auditLogService.logAction(AuditAction.UPDATE, EntityType.DEVELOPER, userId, actorName, payload);
        
        log.info("User profile updated successfully for ID: {}", userId);
        return convertToUserResponseDto(updatedUser);
    }
    
    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponseDto updateUserRole(Long userId, UserRoleUpdateDto roleUpdateDto, String actorName) {
        log.info("Updating role for user ID: {} to role: {}", userId, roleUpdateDto.getRoleName());
        
        User user = findUserById(userId);
        Role newRole = roleRepository.findByName(roleUpdateDto.getRoleName())
                .orElseThrow(() -> new EntityNotFoundException("Role not found: " + roleUpdateDto.getRoleName()));
        
        // Store old roles for audit
        Map<String, Object> oldData = createUserAuditPayload(user);
        
        // Clear existing roles and add new role
        user.getRoles().clear();
        user.addRole(newRole);
        
        User updatedUser = userRepository.save(user);
        
        // Log the role update
        Map<String, Object> payload = new HashMap<>();
        payload.put("action", "ROLE_UPDATE");
        payload.put("oldData", oldData);
        payload.put("newData", createUserAuditPayload(updatedUser));
        
        auditLogService.logAction(AuditAction.UPDATE, EntityType.DEVELOPER, userId, actorName, payload);
        
        log.info("User role updated successfully for ID: {}", userId);
        return convertToUserResponseDto(updatedUser);
    }
    
    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponseDto toggleUserStatus(Long userId, boolean enabled, String actorName) {
        log.info("Toggling user status for ID: {} to enabled: {}", userId, enabled);
        
        User user = findUserById(userId);
        
        // Store old data for audit
        Map<String, Object> oldData = createUserAuditPayload(user);
        
        user.setEnabled(enabled);
        User updatedUser = userRepository.save(user);
        
        // Log the status change
        Map<String, Object> payload = new HashMap<>();
        payload.put("action", enabled ? "USER_ENABLED" : "USER_DISABLED");
        payload.put("oldData", oldData);
        payload.put("newData", createUserAuditPayload(updatedUser));
        
        auditLogService.logAction(AuditAction.UPDATE, EntityType.DEVELOPER, userId, actorName, payload);
        
        log.info("User status updated successfully for ID: {}", userId);
        return convertToUserResponseDto(updatedUser);
    }
    
    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteUser(Long userId, String actorName) {
        log.info("Deleting user with ID: {}", userId);
        
        User user = findUserById(userId);
        
        // Store data for audit before deletion
        Map<String, Object> payload = createUserAuditPayload(user);
        
        // Delete user
        userRepository.delete(user);
        
        // Log the deletion
        auditLogService.logAction(AuditAction.DELETE, EntityType.DEVELOPER, userId, actorName, payload);
        
        log.info("User deleted successfully with ID: {}", userId);
    }
    
    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserResponseDto> getUsersByRole(String roleName) {
        log.debug("Fetching users by role: {}", roleName);
        List<User> users = userRepository.findByRoleName(roleName);
        return users.stream()
                .map(this::convertToUserResponseDto)
                .collect(Collectors.toList());
    }
    
    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Object> getUserStatistics() {
        log.debug("Fetching user statistics");
        
        Map<String, Object> statistics = new HashMap<>();
        
        // Total users
        statistics.put("totalUsers", userRepository.count());
        
        // Users by provider
        List<Object[]> providerStats = userRepository.getUserProviderStatistics();
        Map<String, Long> providerCounts = new HashMap<>();
        for (Object[] stat : providerStats) {
            providerCounts.put(stat[0].toString(), (Long) stat[1]);
        }
        statistics.put("usersByProvider", providerCounts);
        
        // Users by role
        List<Object[]> roleStats = roleRepository.getRoleStatistics();
        Map<String, Long> roleCounts = new HashMap<>();
        for (Object[] stat : roleStats) {
            roleCounts.put((String) stat[0], (Long) stat[1]);
        }
        statistics.put("usersByRole", roleCounts);
        
        // Active vs inactive users
        statistics.put("enabledUsers", userRepository.findByEnabledTrue().size());
        statistics.put("disabledUsers", userRepository.findByEnabledFalse().size());
        
        // Users without roles
        statistics.put("usersWithoutRoles", userRepository.findUsersWithoutRoles().size());
        
        // Recent logins (last 7 days)
        LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);
        statistics.put("recentLogins", userRepository.findByLastLoginAfter(weekAgo).size());
        
        // Never logged in
        statistics.put("neverLoggedIn", userRepository.findByLastLoginIsNull().size());
        
        return statistics;
    }
    
    @Override
    public boolean canAccessUser(Long userId) {
        try {
            User currentUser = getCurrentUserEntity();
            
            // Admins can access any user
            if (currentUser.hasRole("ROLE_ADMIN")) {
                return true;
            }
            
            // Users can access their own profile
            return currentUser.getId().equals(userId);
        } catch (Exception e) {
            log.warn("Error checking user access: {}", e.getMessage());
            return false;
        }
    }
    
    // Helper methods
    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + userId));
    }
    
    private UserResponseDto convertToUserResponseDto(User user) {
        UserResponseDto dto = new UserResponseDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setFullName(user.getFullName());
        dto.setEnabled(user.isEnabled());
        dto.setProvider(user.getProvider());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setLastLogin(user.getLastLogin());

        // Convert roles to string set
        dto.setRoles(user.getRoles().stream()
                .map(role -> role.getName())
                .collect(Collectors.toSet()));

        return dto;
    }

    private Map<String, Object> createUserAuditPayload(User user) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("id", user.getId());
        payload.put("username", user.getUsername());
        payload.put("email", user.getEmail());
        payload.put("fullName", user.getFullName());
        payload.put("enabled", user.isEnabled());
        payload.put("provider", user.getProvider());
        payload.put("roles", user.getRoles().stream()
                .map(role -> role.getName())
                .collect(Collectors.toSet()));
        return payload;
    }
}