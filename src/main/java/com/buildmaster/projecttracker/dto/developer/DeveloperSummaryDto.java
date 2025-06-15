package com.buildmaster.projecttracker.dto.developer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Summary DTO for developer info in other contexts
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeveloperSummaryDto {
    private Long id;
    private String name;
    private String email;
    private int taskCount;
}
