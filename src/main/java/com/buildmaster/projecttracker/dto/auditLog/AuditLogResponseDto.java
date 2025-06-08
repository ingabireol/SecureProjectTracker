package com.buildmaster.projecttracker.dto.auditLog;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogResponseDto {
    private String id;
    private String actionType;
    private String entityType;
    private Long entityId;
    private LocalDateTime timestamp;
    private String actorName;
    private Map<String, Object> payload;
}
