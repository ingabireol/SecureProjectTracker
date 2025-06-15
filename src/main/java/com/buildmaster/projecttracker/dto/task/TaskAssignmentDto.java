package com.buildmaster.projecttracker.dto.task;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskAssignmentDto {
    @NotNull(message = "Task ID is required")
    private Long taskId;
    
    private Long developerId; // null to unassign
}