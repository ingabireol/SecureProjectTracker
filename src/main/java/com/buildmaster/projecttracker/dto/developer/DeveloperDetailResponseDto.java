package com.buildmaster.projecttracker.dto.developer;

import com.buildmaster.projecttracker.dto.project.TaskSummaryDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeveloperDetailResponseDto {
    private Long id;
    private String name;
    private String email;
    private Set<String> skills;
    private List<TaskSummaryDto> assignedTasks;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}