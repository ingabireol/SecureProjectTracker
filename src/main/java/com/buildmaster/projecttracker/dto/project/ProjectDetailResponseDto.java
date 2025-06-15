package com.buildmaster.projecttracker.dto.project;

import com.buildmaster.projecttracker.model.ProjectStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectDetailResponseDto {
    private Long id;
    private String name;
    private String description;
    private LocalDateTime deadline;
    private ProjectStatus status;
    private List<TaskSummaryDto> tasks;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}