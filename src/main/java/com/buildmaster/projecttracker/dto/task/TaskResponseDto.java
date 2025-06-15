package com.buildmaster.projecttracker.dto.task;

import com.buildmaster.projecttracker.model.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskResponseDto {
    private Long id;
    private String title;
    private String description;
    private TaskStatus status;
    private LocalDateTime dueDate;
    private boolean isOverdue;
    private String projectName;
    private Long projectId;
    private String assignedDeveloperName;
    private Long assignedDeveloperId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
