package com.buildmaster.projecttracker.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// User role update DTO (for admin operations)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "User role update request")
public class UserRoleUpdateDto {
    
    @NotBlank(message = "Role name is required")
    @Size(min = 2, max = 50, message = "Role name must be between 2 and 50 characters")
    @Schema(description = "New role name", example = "ROLE_MANAGER")
    private String roleName;
}