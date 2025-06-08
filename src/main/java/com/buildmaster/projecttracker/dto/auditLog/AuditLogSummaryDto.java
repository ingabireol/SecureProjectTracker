package com.buildmaster.projecttracker.dto.auditLog;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogSummaryDto {
    private String id;
    private String actionType;
    private String entityType;
    private Long entityId;
    private LocalDateTime timestamp;
    private String actorName;
}