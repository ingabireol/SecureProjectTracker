package com.buildmaster.projecttracker.controller;

import com.buildmaster.projecttracker.dto.user.UserResponseDto;
import com.buildmaster.projecttracker.dto.user.UserRoleUpdateDto;
import com.buildmaster.projecttracker.dto.user.UserUpdateDto;
import com.buildmaster.projecttracker.model.user.User;
import com.buildmaster.projecttracker.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
@Tag(name = "User Management", description = "User profile and management operations")
@SecurityRequirement(name = "bearerAuth")
public class UserController {
    
    private final UserService userService;
    
    @GetMapping("/me")
    @Operation(summary = "Get current user profile", description = "Get the profile of the currently authenticated user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User profile retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "User not authenticated")
    })
    public ResponseEntity<UserResponseDto> getCurrentUserProfile() {
        log.debug("Getting current user profile");
        UserResponseDto user = userService.getCurrentUser();
        return ResponseEntity.ok(user);
    }
    
    @PutMapping("/me")
    @Operation(summary = "Update current user profile", description = "Update the profile of the currently authenticated user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Profile updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid update data"),
        @ApiResponse(responseCode = "409", description = "Email already in use")
    })
    public ResponseEntity<UserResponseDto> updateCurrentUserProfile(
            @Valid @RequestBody @Parameter(description = "Profile update data") UserUpdateDto updateDto,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        User user = (User) userDetails;
        log.info("Updating profile for user: {}", user.getUsername());
        
        UserResponseDto updatedUser = userService.updateUserProfile(user.getId(), updateDto, user.getUsername());
        return ResponseEntity.ok(updatedUser);
    }
    
    @GetMapping("/{userId}")
    @Operation(summary = "Get user by ID", description = "Get user information by ID (Admin or own profile only)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User found"),
        @ApiResponse(responseCode = "403", description = "Access denied"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<UserResponseDto> getUserById(
            @PathVariable @Parameter(description = "User ID") Long userId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        User currentUser = (User) userDetails;
        log.debug("Getting user {} requested by {}", userId, currentUser.getUsername());
        
        UserResponseDto user = userService.getUserById(userId);
        return ResponseEntity.ok(user);
    }
    
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all users", description = "Get paginated list of all users (Admin only)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Users retrieved successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied - Admin role required")
    })
    public ResponseEntity<Page<UserResponseDto>> getAllUsers(
            @PageableDefault(size = 20, sort = "username") Pageable pageable) {
        
        log.debug("Getting all users with pagination");
        Page<UserResponseDto> users = userService.getAllUsers(pageable);
        return ResponseEntity.ok(users);
    }
    
    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Search users", description = "Search users by name, email, or username (Admin only)")
    public ResponseEntity<Page<UserResponseDto>> searchUsers(
            @RequestParam @Parameter(description = "Search term") String searchTerm,
            @PageableDefault(size = 20, sort = "username") Pageable pageable) {
        
        log.debug("Searching users with term: {}", searchTerm);
        Page<UserResponseDto> users = userService.searchUsers(searchTerm, pageable);
        return ResponseEntity.ok(users);
    }
    
    @PutMapping("/{userId}/role")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update user role", description = "Update user's role (Admin only)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Role updated successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied - Admin role required"),
        @ApiResponse(responseCode = "404", description = "User or role not found")
    })
    public ResponseEntity<UserResponseDto> updateUserRole(
            @PathVariable @Parameter(description = "User ID") Long userId,
            @Valid @RequestBody @Parameter(description = "New role information") UserRoleUpdateDto roleUpdateDto,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        User admin = (User) userDetails;
        log.info("Admin {} updating role for user {} to {}", admin.getUsername(), userId, roleUpdateDto.getRoleName());
        
        UserResponseDto updatedUser = userService.updateUserRole(userId, roleUpdateDto, admin.getUsername());
        return ResponseEntity.ok(updatedUser);
    }
    
    @PutMapping("/{userId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Toggle user status", description = "Enable or disable user account (Admin only)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User status updated successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied - Admin role required"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<UserResponseDto> toggleUserStatus(
            @PathVariable @Parameter(description = "User ID") Long userId,
            @RequestParam @Parameter(description = "Enable or disable user") boolean enabled,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        User admin = (User) userDetails;
        log.info("Admin {} {} user {}", admin.getUsername(), enabled ? "enabling" : "disabling", userId);
        
        UserResponseDto updatedUser = userService.toggleUserStatus(userId, enabled, admin.getUsername());
        return ResponseEntity.ok(updatedUser);
    }
    
    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete user", description = "Delete user account (Admin only)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "User deleted successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied - Admin role required"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<Void> deleteUser(
            @PathVariable @Parameter(description = "User ID") Long userId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        User admin = (User) userDetails;
        log.info("Admin {} deleting user {}", admin.getUsername(), userId);
        
        userService.deleteUser(userId, admin.getUsername());
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/role/{roleName}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get users by role", description = "Get all users with specific role (Admin only)")
    public ResponseEntity<List<UserResponseDto>> getUsersByRole(
            @PathVariable @Parameter(description = "Role name") String roleName) {
        
        log.debug("Getting users with role: {}", roleName);
        List<UserResponseDto> users = userService.getUsersByRole(roleName);
        return ResponseEntity.ok(users);
    }
    
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get user statistics", description = "Get comprehensive user statistics (Admin only)")
    public ResponseEntity<Map<String, Object>> getUserStatistics() {
        log.debug("Getting user statistics");
        Map<String, Object> statistics = userService.getUserStatistics();
        return ResponseEntity.ok(statistics);
    }
    
    @GetMapping("/profile/access-check/{userId}")
    @Operation(summary = "Check user access", description = "Check if current user can access specific user profile")
    public ResponseEntity<Map<String, Boolean>> checkUserAccess(
            @PathVariable @Parameter(description = "User ID to check access for") Long userId) {
        
        boolean canAccess = userService.canAccessUser(userId);
        Map<String, Boolean> response = new HashMap<>();
        response.put("canAccess", canAccess);
        
        return ResponseEntity.ok(response);
    }
}