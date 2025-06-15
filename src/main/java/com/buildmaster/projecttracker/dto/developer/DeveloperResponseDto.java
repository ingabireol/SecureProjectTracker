package com.buildmaster.projecttracker.dto.developer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

// Response DTO for developer data
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeveloperResponseDto {
    private Long id;
    private String name;
    private String email;
    private Set<String> skills;
    private int taskCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
