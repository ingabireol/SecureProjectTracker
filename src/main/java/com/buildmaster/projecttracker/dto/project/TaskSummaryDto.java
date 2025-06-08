package com.buildmaster.projecttracker.dto.project;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskSummaryDto {
    private Long id;
    private String title;
    private String status;
    private LocalDateTime dueDate;
    private String assignedDeveloperName;
}