package com.buildmaster.projecttracker.service.impl;

import com.buildmaster.projecttracker.dto.auditLog.AuditLogResponseDto;
import com.buildmaster.projecttracker.dto.auditLog.AuditLogSummaryDto;
import com.buildmaster.projecttracker.model.AuditAction;
import com.buildmaster.projecttracker.model.AuditLog;
import com.buildmaster.projecttracker.model.EntityType;
import com.buildmaster.projecttracker.repository.AuditLogRepository;
import com.buildmaster.projecttracker.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;

    @Override
    public void logAction(AuditAction actionType, EntityType entityType, Long entityId,
                          String actorName, Map<String, Object> payload) {
        try {
            log.debug("Logging action: {} for entity: {} with ID: {} by actor: {}",
                    actionType, entityType, entityId, actorName);

            AuditLog auditLog = new AuditLog(actionType, entityType, entityId, actorName, payload);
            auditLogRepository.save(auditLog);

            log.debug("Audit log saved successfully with ID: {}", auditLog.getId());
        } catch (Exception e) {
            log.error("Failed to save audit log for action: {} on entity: {} with ID: {}",
                    actionType, entityType, entityId, e);
            // Don't throw exception to avoid breaking the main operation
        }
    }

    @Override
    public Page<AuditLogSummaryDto> getAllLogs(Pageable pageable) {
        log.debug("Fetching all audit logs with pagination");
        Page<AuditLog> logs = auditLogRepository.findAll(pageable);
        return logs.map(this::convertToSummaryDto);
    }

    @Override
    public Page<AuditLogSummaryDto> getLogsByEntityType(EntityType entityType, Pageable pageable) {
        log.debug("Fetching audit logs by entity type: {}", entityType);
        Page<AuditLog> logs = auditLogRepository.findByEntityType(entityType, pageable);
        return logs.map(this::convertToSummaryDto);
    }

    @Override
    public Page<AuditLogSummaryDto> getLogsByActionType(AuditAction actionType, Pageable pageable) {
        log.debug("Fetching audit logs by action type: {}", actionType);
        Page<AuditLog> logs = auditLogRepository.findByActionType(actionType, pageable);
        return logs.map(this::convertToSummaryDto);
    }

    @Override
    public Page<AuditLogSummaryDto> getLogsByActor(String actorName, Pageable pageable) {
        log.debug("Fetching audit logs by actor: {}", actorName);
        Page<AuditLog> logs = auditLogRepository.findByActorName(actorName, pageable);
        return logs.map(this::convertToSummaryDto);
    }

    @Override
    public List<AuditLogResponseDto> getLogsForEntity(EntityType entityType, Long entityId) {
        log.debug("Fetching audit logs for entity: {} with ID: {}", entityType, entityId);
        List<AuditLog> logs = auditLogRepository.findByEntityTypeAndEntityIdOrderByTimestampDesc(entityType, entityId);
        return logs.stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<AuditLogResponseDto> getLogsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        log.debug("Fetching audit logs by date range: {} to {}", startDate, endDate);
        List<AuditLog> logs = auditLogRepository.findByTimestampBetweenOrderByTimestampDesc(startDate, endDate);
        return logs.stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<AuditLogResponseDto> getRecentLogs(int days) {
        log.debug("Fetching recent audit logs for last {} days", days);
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(days);
        List<AuditLog> logs = auditLogRepository.findRecentLogs(cutoffDate);
        return logs.stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public Page<AuditLogSummaryDto> searchLogs(EntityType entityType, AuditAction actionType,
                                               String actorName, LocalDateTime startDate,
                                               LocalDateTime endDate, Pageable pageable) {
        log.debug("Searching audit logs with criteria - entity: {}, action: {}, actor: {}, dates: {} to {}",
                entityType, actionType, actorName, startDate, endDate);

        // Use current time if end date is null
        LocalDateTime searchEndDate = endDate != null ? endDate : LocalDateTime.now();
        // Use a reasonable default start date if null (e.g., 30 days ago)
        LocalDateTime searchStartDate = startDate != null ? startDate : LocalDateTime.now().minusDays(30);

        Page<AuditLog> logs = auditLogRepository.findLogsByCriteria(
                entityType, actionType, actorName, searchStartDate, searchEndDate, pageable);
        return logs.map(this::convertToSummaryDto);
    }

    @Override
    public Map<String, Long> getAuditStatistics() {
        log.debug("Fetching audit statistics");
        Map<String, Long> statistics = new HashMap<>();

        statistics.put("totalLogs", auditLogRepository.count());
        statistics.put("recentLogs7Days", (long) getRecentLogs(7).size());
        statistics.put("recentLogs30Days", (long) getRecentLogs(30).size());

        return statistics;
    }

    @Override
    public Map<String, Long> getEntityTypeStatistics() {
        log.debug("Fetching entity type statistics");
        Map<String, Long> statistics = new HashMap<>();

        statistics.put("PROJECT", auditLogRepository.countByEntityType(EntityType.PROJECT));
        statistics.put("TASK", auditLogRepository.countByEntityType(EntityType.TASK));
        statistics.put("DEVELOPER", auditLogRepository.countByEntityType(EntityType.DEVELOPER));

        return statistics;
    }

    @Override
    public Map<String, Long> getActionTypeStatistics() {
        log.debug("Fetching action type statistics");
        Map<String, Long> statistics = new HashMap<>();

        statistics.put("CREATE", auditLogRepository.countByActionType(AuditAction.CREATE));
        statistics.put("UPDATE", auditLogRepository.countByActionType(AuditAction.UPDATE));
        statistics.put("DELETE", auditLogRepository.countByActionType(AuditAction.DELETE));

        return statistics;
    }

    @Override
    public Map<String, Long> getActorStatistics() {
        log.debug("Fetching actor statistics");
        Map<String, Long> statistics = new HashMap<>();

        // This is a simplified implementation
        // In a real application, you might want to implement a more sophisticated query
        List<AuditLog> allLogs = auditLogRepository.findAll();
        Map<String, Long> actorCounts = allLogs.stream()
                .collect(Collectors.groupingBy(
                        AuditLog::getActorName,
                        Collectors.counting()
                ));

        return actorCounts;
    }

    @Override
    public long cleanupOldLogs(int retentionDays) {
        log.info("Cleaning up audit logs older than {} days", retentionDays);
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(retentionDays);
        long deletedCount = auditLogRepository.deleteByTimestampBefore(cutoffDate);
        log.info("Deleted {} old audit logs", deletedCount);
        return deletedCount;
    }

    @Override
    public AuditLogResponseDto getLogById(String logId) {
        log.debug("Fetching audit log by ID: {}", logId);
        AuditLog auditLog = auditLogRepository.findById(logId)
                .orElseThrow(() -> new RuntimeException("Audit log not found with ID: " + logId));
        return convertToResponseDto(auditLog);
    }

    // Helper methods
    private AuditLogResponseDto convertToResponseDto(AuditLog auditLog) {
        AuditLogResponseDto dto = new AuditLogResponseDto();
        dto.setId(auditLog.getId());
        dto.setActionType(auditLog.getActionType());
        dto.setEntityType(auditLog.getEntityType());
        dto.setEntityId(auditLog.getEntityId());
        dto.setTimestamp(auditLog.getTimestamp());
        dto.setActorName(auditLog.getActorName());
        dto.setPayload(auditLog.getPayload());
        return dto;
    }

    private AuditLogSummaryDto convertToSummaryDto(AuditLog auditLog) {
        AuditLogSummaryDto dto = new AuditLogSummaryDto();
        dto.setId(auditLog.getId());
        dto.setActionType(auditLog.getActionType());
        dto.setEntityType(auditLog.getEntityType());
        dto.setEntityId(auditLog.getEntityId());
        dto.setTimestamp(auditLog.getTimestamp());
        dto.setActorName(auditLog.getActorName());
        return dto;
    }
}