package com.buildmaster.projecttracker.dto.task;

import com.buildmaster.projecttracker.model.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// Request DTO for creating/updating tasks
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskRequestDto {
    
    @NotBlank(message = "Task title is required")
    @Size(min = 3, max = 100, message = "Title must be between 3 and 100 characters")
    private String title;
    
    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;
    
    private TaskStatus status;
    
    private LocalDateTime dueDate;
    
    @NotNull(message = "Project ID is required")
    private Long projectId;
    
    private Long assignedDeveloperId;
}
