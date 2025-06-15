package com.buildmaster.projecttracker.dto.project;

import com.buildmaster.projecttracker.model.ProjectStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
// Request DTO for creating/updating projects
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectRequestDto {
    
    @NotBlank(message = "Project name is required")
    @Size(min = 3, max = 100, message = "Project name must be between 3 and 100 characters")
    private String name;
    
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;
    
    @NotNull(message = "Deadline is required")
    private LocalDateTime deadline;
    
    private ProjectStatus status;
}
