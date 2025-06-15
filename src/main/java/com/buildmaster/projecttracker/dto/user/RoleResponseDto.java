package com.buildmaster.projecttracker.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoleResponseDto {
    
    private Long id;
    
    private String name;
    
    private String description;
    
    private int userCount;
    
    private LocalDateTime createdAt;
}