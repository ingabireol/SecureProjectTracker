package com.buildmaster.projecttracker.dto.project;

import com.buildmaster.projecttracker.model.ProjectStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectResponseDto {
    private Long id;
    private String name;
    private String description;
    private LocalDateTime deadline;
    private ProjectStatus status;
    private int taskCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}