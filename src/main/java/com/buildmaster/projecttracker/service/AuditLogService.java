package com.buildmaster.projecttracker.service;

import com.buildmaster.projecttracker.dto.auditLog.AuditLogResponseDto;
import com.buildmaster.projecttracker.dto.auditLog.AuditLogSummaryDto;
import com.buildmaster.projecttracker.model.AuditAction;
import com.buildmaster.projecttracker.model.EntityType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface AuditLogService {

    /**
     * Log an entity action (CREATE, UPDATE, DELETE)
     */
    void logAction(AuditAction actionType, EntityType entityType, Long entityId,
                   String actorName, Map<String, Object> payload);

    /**
     * Get all audit logs with pagination
     */
    Page<AuditLogSummaryDto> getAllLogs(Pageable pageable);

    /**
     * Get logs by entity type
     */
    Page<AuditLogSummaryDto> getLogsByEntityType(EntityType entityType, Pageable pageable);

    /**
     * Get logs by action type
     */
    Page<AuditLogSummaryDto> getLogsByActionType(AuditAction actionType, Pageable pageable);

    /**
     * Get logs by actor name
     */
    Page<AuditLogSummaryDto> getLogsByActor(String actorName, Pageable pageable);

    /**
     * Get logs for a specific entity
     */
    List<AuditLogResponseDto> getLogsForEntity(EntityType entityType, Long entityId);

    /**
     * Get logs within date range
     */
    List<AuditLogResponseDto> getLogsByDateRange(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Get recent logs (last N days)
     */
    List<AuditLogResponseDto> getRecentLogs(int days);

    /**
     * Search logs by multiple criteria
     */
    Page<AuditLogSummaryDto> searchLogs(EntityType entityType, AuditAction actionType,
                                        String actorName, LocalDateTime startDate,
                                        LocalDateTime endDate, Pageable pageable);

    /**
     * Get audit statistics
     */
    Map<String, Long> getAuditStatistics();

    /**
     * Get entity type statistics
     */
    Map<String, Long> getEntityTypeStatistics();

    /**
     * Get action type statistics
     */
    Map<String, Long> getActionTypeStatistics();

    /**
     * Get actor activity statistics
     */
    Map<String, Long> getActorStatistics();

    /**
     * Clean up old audit logs (older than specified days)
     */
    long cleanupOldLogs(int retentionDays);

    /**
     * Get detailed audit log by ID
     */
    AuditLogResponseDto getLogById(String logId);
}