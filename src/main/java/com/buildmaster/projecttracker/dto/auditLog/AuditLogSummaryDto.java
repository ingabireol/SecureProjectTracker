package com.buildmaster.projecttracker.dto.auditLog;

import com.buildmaster.projecttracker.model.AuditAction;
import com.buildmaster.projecttracker.model.EntityType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogSummaryDto {
    private String id;
    private AuditAction actionType;
    private EntityType entityType;
    private Long entityId;
    private LocalDateTime timestamp;
    private String actorName;
}