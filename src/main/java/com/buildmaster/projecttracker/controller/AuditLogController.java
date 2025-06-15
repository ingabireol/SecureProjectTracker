package com.buildmaster.projecttracker.controller;

import com.buildmaster.projecttracker.dto.auditLog.AuditLogResponseDto;
import com.buildmaster.projecttracker.dto.auditLog.AuditLogSummaryDto;
import com.buildmaster.projecttracker.model.AuditAction;
import com.buildmaster.projecttracker.model.EntityType;
import com.buildmaster.projecttracker.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/audit-logs")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class AuditLogController {
    
    private final AuditLogService auditLogService;
    
    @GetMapping
    public ResponseEntity<Page<AuditLogSummaryDto>> getAllLogs(
            @PageableDefault(size = 20, sort = "timestamp", direction = org.springframework.data.domain.Sort.Direction.DESC) 
            Pageable pageable) {
        log.debug("Fetching all audit logs with pagination");
        Page<AuditLogSummaryDto> logs = auditLogService.getAllLogs(pageable);
        return ResponseEntity.ok(logs);
    }
    
    @GetMapping("/{logId}")
    public ResponseEntity<AuditLogResponseDto> getLogById(@PathVariable String logId) {
        log.debug("Fetching audit log by ID: {}", logId);
        AuditLogResponseDto log = auditLogService.getLogById(logId);
        return ResponseEntity.ok(log);
    }
    
    @GetMapping("/entity-type/{entityType}")
    public ResponseEntity<Page<AuditLogSummaryDto>> getLogsByEntityType(
            @PathVariable EntityType entityType,
            @PageableDefault(size = 20, sort = "timestamp", direction = org.springframework.data.domain.Sort.Direction.DESC) 
            Pageable pageable) {
        log.debug("Fetching audit logs by entity type: {}", entityType);
        Page<AuditLogSummaryDto> logs = auditLogService.getLogsByEntityType(entityType, pageable);
        return ResponseEntity.ok(logs);
    }
    
    @GetMapping("/action-type/{actionType}")
    public ResponseEntity<Page<AuditLogSummaryDto>> getLogsByActionType(
            @PathVariable AuditAction actionType,
            @PageableDefault(size = 20, sort = "timestamp", direction = org.springframework.data.domain.Sort.Direction.DESC) 
            Pageable pageable) {
        log.debug("Fetching audit logs by action type: {}", actionType);
        Page<AuditLogSummaryDto> logs = auditLogService.getLogsByActionType(actionType, pageable);
        return ResponseEntity.ok(logs);
    }
    
    @GetMapping("/actor/{actorName}")
    public ResponseEntity<Page<AuditLogSummaryDto>> getLogsByActor(
            @PathVariable String actorName,
            @PageableDefault(size = 20, sort = "timestamp", direction = org.springframework.data.domain.Sort.Direction.DESC) 
            Pageable pageable) {
        log.debug("Fetching audit logs by actor: {}", actorName);
        Page<AuditLogSummaryDto> logs = auditLogService.getLogsByActor(actorName, pageable);
        return ResponseEntity.ok(logs);
    }
    
    @GetMapping("/entity/{entityType}/{entityId}")
    public ResponseEntity<List<AuditLogResponseDto>> getLogsForEntity(
            @PathVariable EntityType entityType,
            @PathVariable Long entityId) {
        log.debug("Fetching audit logs for entity: {} with ID: {}", entityType, entityId);
        List<AuditLogResponseDto> logs = auditLogService.getLogsForEntity(entityType, entityId);
        return ResponseEntity.ok(logs);
    }
    
    @GetMapping("/date-range")
    public ResponseEntity<List<AuditLogResponseDto>> getLogsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        log.debug("Fetching audit logs by date range: {} to {}", startDate, endDate);
        List<AuditLogResponseDto> logs = auditLogService.getLogsByDateRange(startDate, endDate);
        return ResponseEntity.ok(logs);
    }
    
    @GetMapping("/recent")
    public ResponseEntity<List<AuditLogResponseDto>> getRecentLogs(
            @RequestParam(defaultValue = "7") int days) {
        log.debug("Fetching recent audit logs for last {} days", days);
        List<AuditLogResponseDto> logs = auditLogService.getRecentLogs(days);
        return ResponseEntity.ok(logs);
    }
    
    @GetMapping("/search")
    public ResponseEntity<Page<AuditLogSummaryDto>> searchLogs(
            @RequestParam(required = false) EntityType entityType,
            @RequestParam(required = false) AuditAction actionType,
            @RequestParam(required = false) String actorName,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @PageableDefault(size = 20, sort = "timestamp", direction = org.springframework.data.domain.Sort.Direction.DESC) 
            Pageable pageable) {
        log.debug("Searching audit logs with criteria - entity: {}, action: {}, actor: {}, dates: {} to {}", 
                 entityType, actionType, actorName, startDate, endDate);
        Page<AuditLogSummaryDto> logs = auditLogService.searchLogs(entityType, actionType, actorName, startDate, endDate, pageable);
        return ResponseEntity.ok(logs);
    }
    
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Long>> getAuditStatistics() {
        log.debug("Fetching audit statistics");
        Map<String, Long> statistics = auditLogService.getAuditStatistics();
        return ResponseEntity.ok(statistics);
    }
    
    @GetMapping("/statistics/entity-types")
    public ResponseEntity<Map<String, Long>> getEntityTypeStatistics() {
        log.debug("Fetching entity type statistics");
        Map<String, Long> statistics = auditLogService.getEntityTypeStatistics();
        return ResponseEntity.ok(statistics);
    }
    
    @GetMapping("/statistics/action-types")
    public ResponseEntity<Map<String, Long>> getActionTypeStatistics() {
        log.debug("Fetching action type statistics");
        Map<String, Long> statistics = auditLogService.getActionTypeStatistics();
        return ResponseEntity.ok(statistics);
    }
    
    @GetMapping("/statistics/actors")
    public ResponseEntity<Map<String, Long>> getActorStatistics() {
        log.debug("Fetching actor statistics");
        Map<String, Long> statistics = auditLogService.getActorStatistics();
        return ResponseEntity.ok(statistics);
    }
    
    @DeleteMapping("/cleanup")
    public ResponseEntity<Map<String, Object>> cleanupOldLogs(
            @RequestParam(defaultValue = "90") int retentionDays,
            @RequestHeader(value = "X-Actor-Name", defaultValue = "system") String actorName) {
        log.info("Cleaning up audit logs older than {} days by actor: {}", retentionDays, actorName);
        long deletedCount = auditLogService.cleanupOldLogs(retentionDays);
        Map<String, Object> result = Map.of(
            "deletedCount", deletedCount,
            "retentionDays", retentionDays,
            "cleanedBy", actorName,
            "cleanupTime", LocalDateTime.now()
        );
        return ResponseEntity.ok(result);
    }
}