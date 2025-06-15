package com.buildmaster.projecttracker.controller;

import com.buildmaster.projecttracker.dto.user.RoleResponseDto;
import com.buildmaster.projecttracker.dto.user.UserResponseDto;
import com.buildmaster.projecttracker.model.role.Role;
import com.buildmaster.projecttracker.model.user.User;
import com.buildmaster.projecttracker.repository.RoleRepository;
import com.buildmaster.projecttracker.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
@CrossOrigin(origins = "*")
@Tag(name = "Admin Operations", description = "Administrative operations (Admin role required)")
@SecurityRequirement(name = "bearerAuth")
public class AdminController {
    
    private final UserService userService;
    private final RoleRepository roleRepository;
    
    @GetMapping("/dashboard")
    @Operation(summary = "Get admin dashboard data", description = "Get comprehensive system statistics for admin dashboard")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Dashboard data retrieved successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied - Admin role required")
    })
    public ResponseEntity<Map<String, Object>> getAdminDashboard() {
        log.debug("Getting admin dashboard data");
        
        Map<String, Object> dashboard = new HashMap<>();
        
        // User statistics
        Map<String, Object> userStats = userService.getUserStatistics();
        dashboard.put("userStatistics", userStats);
        
        // Role statistics  
        List<Object[]> roleStats = roleRepository.getRoleStatistics();
        Map<String, Object> roleData = new HashMap<>();
        for (Object[] stat : roleStats) {
            roleData.put((String) stat[0], stat[1]);
        }
        dashboard.put("roleStatistics", roleData);
        
        // System info
        Map<String, Object> systemInfo = new HashMap<>();
        systemInfo.put("timestamp", LocalDateTime.now());
        systemInfo.put("totalRoles", roleRepository.count());
        dashboard.put("systemInfo", systemInfo);
        
        return ResponseEntity.ok(dashboard);
    }
    
    @GetMapping("/users")
    @Operation(summary = "Get all users for admin", description = "Get paginated list of all users with admin view")
    public ResponseEntity<Page<UserResponseDto>> getAllUsersForAdmin(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable,
            @RequestParam(required = false) @Parameter(description = "Filter by role") String role,
            @RequestParam(required = false) @Parameter(description = "Filter by enabled status") Boolean enabled) {
        
        log.debug("Admin getting all users - role filter: {}, enabled filter: {}", role, enabled);
        
        if (role != null) {
            List<UserResponseDto> users = userService.getUsersByRole(role);
            // Convert to Page (simplified for demo - in production, implement proper pagination)
            return ResponseEntity.ok(new org.springframework.data.domain.PageImpl<>(users, pageable, users.size()));
        }
        
        Page<UserResponseDto> users = userService.getAllUsers(pageable);
        return ResponseEntity.ok(users);
    }
    
    @GetMapping("/users/search")
    @Operation(summary = "Advanced user search", description = "Advanced search with multiple criteria")
    public ResponseEntity<Page<UserResponseDto>> advancedUserSearch(
            @RequestParam @Parameter(description = "Search term") String searchTerm,
            @PageableDefault(size = 20, sort = "username") Pageable pageable) {
        
        log.debug("Admin performing advanced user search: {}", searchTerm);
        Page<UserResponseDto> users = userService.searchUsers(searchTerm, pageable);
        return ResponseEntity.ok(users);
    }
    
    @GetMapping("/roles")
    @Operation(summary = "Get all roles", description = "Get list of all available roles in the system")
    public ResponseEntity<List<RoleResponseDto>> getAllRoles() {
        log.debug("Getting all roles");
        
        List<Role> roles = roleRepository.findAllByOrderByNameAsc();
        List<RoleResponseDto> roleResponses = roles.stream()
                .map(this::convertToRoleResponseDto)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(roleResponses);
    }
    
    @GetMapping("/roles/statistics")
    @Operation(summary = "Get role statistics", description = "Get statistics about role usage")
    public ResponseEntity<Map<String, Object>> getRoleStatistics() {
        log.debug("Getting role statistics");
        
        Map<String, Object> statistics = new HashMap<>();
        
        List<Object[]> roleStats = roleRepository.getRoleStatistics();
        Map<String, Long> roleCounts = new HashMap<>();
        for (Object[] stat : roleStats) {
            roleCounts.put((String) stat[0], (Long) stat[1]);
        }
        
        statistics.put("roleUserCounts", roleCounts);
        statistics.put("totalRoles", roleRepository.count());
        statistics.put("rolesWithUsers", roleRepository.findRolesWithUsers().size());
        statistics.put("rolesWithoutUsers", roleRepository.findRolesWithoutUsers().size());
        
        return ResponseEntity.ok(statistics);
    }
    
    @PostMapping("/users/{userId}/force-logout")
    @Operation(summary = "Force user logout", description = "Force logout a user by disabling their account temporarily")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User logged out successfully"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<Map<String, String>> forceUserLogout(
            @PathVariable @Parameter(description = "User ID") Long userId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        User admin = (User) userDetails;
        log.info("Admin {} forcing logout for user {}", admin.getUsername(), userId);
        
        // Disable user temporarily (they can be re-enabled)
        userService.toggleUserStatus(userId, false, admin.getUsername());
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "User has been logged out and account disabled");
        response.put("userId", userId.toString());
        response.put("performedBy", admin.getUsername());
        response.put("timestamp", LocalDateTime.now().toString());
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/audit/summary")
    @Operation(summary = "Get audit summary", description = "Get summary of recent audit activities")
    public ResponseEntity<Map<String, Object>> getAuditSummary() {
        log.debug("Getting audit summary for admin");
        
        Map<String, Object> auditSummary = new HashMap<>();
        
        // User statistics from audit perspective
        Map<String, Object> userStats = userService.getUserStatistics();
        auditSummary.put("recentLogins", userStats.get("recentLogins"));
        auditSummary.put("neverLoggedIn", userStats.get("neverLoggedIn"));
        auditSummary.put("totalUsers", userStats.get("totalUsers"));
        
        // Add timestamp
        auditSummary.put("generatedAt", LocalDateTime.now());
        
        return ResponseEntity.ok(auditSummary);
    }
    
    @PostMapping("/maintenance/cleanup-disabled-users")
    @Operation(summary = "Cleanup disabled users", description = "Remove users that have been disabled for a long time")
    public ResponseEntity<Map<String, Object>> cleanupDisabledUsers(
            @RequestParam(defaultValue = "30") @Parameter(description = "Days since disabled") int daysDisabled,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        User admin = (User) userDetails;
        log.info("Admin {} initiating cleanup of disabled users older than {} days", admin.getUsername(), daysDisabled);
        
        // This is a placeholder - implement actual cleanup logic based on your needs
        Map<String, Object> result = new HashMap<>();
        result.put("message", "Cleanup operation completed");
        result.put("daysDisabled", daysDisabled);
        result.put("performedBy", admin.getUsername());
        result.put("timestamp", LocalDateTime.now());
        result.put("usersProcessed", 0); // Placeholder
        
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/system/health")
    @Operation(summary = "Get system health", description = "Get comprehensive system health information")
    public ResponseEntity<Map<String, Object>> getSystemHealth() {
        log.debug("Getting system health information");
        
        Map<String, Object> health = new HashMap<>();
        
        // Database health
        Map<String, Object> dbHealth = new HashMap<>();
        try {
            long userCount = userService.getUserStatistics().get("totalUsers") != null ? 
                (Long) userService.getUserStatistics().get("totalUsers") : 0;
            dbHealth.put("status", "UP");
            dbHealth.put("userCount", userCount);
            dbHealth.put("roleCount", roleRepository.count());
        } catch (Exception e) {
            dbHealth.put("status", "DOWN");
            dbHealth.put("error", e.getMessage());
        }
        
        health.put("database", dbHealth);
        health.put("timestamp", LocalDateTime.now());
        health.put("uptime", "N/A"); // Placeholder - implement actual uptime tracking
        
        return ResponseEntity.ok(health);
    }
    
    // Helper method to convert Role to RoleResponseDto
    private RoleResponseDto convertToRoleResponseDto(Role role) {
        RoleResponseDto dto = new RoleResponseDto();
        dto.setId(role.getId());
        dto.setName(role.getName());
        dto.setDescription(role.getDescription());
        dto.setUserCount(role.getUsers() != null ? role.getUsers().size() : 0);
        dto.setCreatedAt(role.getCreatedAt());
        return dto;
    }
}