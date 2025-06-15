package com.buildmaster.projecttracker.repository;

import com.buildmaster.projecttracker.model.AuditAction;
import com.buildmaster.projecttracker.model.AuditLog;
import com.buildmaster.projecttracker.model.EntityType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends MongoRepository<AuditLog, String> {

    // Find logs by entity type
    Page<AuditLog> findByEntityType(EntityType entityType, Pageable pageable);

    // Find logs by action type
    Page<AuditLog> findByActionType(AuditAction actionType, Pageable pageable);

    // Find logs by actor name
    Page<AuditLog> findByActorName(String actorName, Pageable pageable);

    // Find logs by entity type and entity ID
    List<AuditLog> findByEntityTypeAndEntityIdOrderByTimestampDesc(EntityType entityType, Long entityId);

    // Find logs by timestamp range
    List<AuditLog> findByTimestampBetweenOrderByTimestampDesc(LocalDateTime startDate, LocalDateTime endDate);

    // Find logs by entity type and action type
    Page<AuditLog> findByEntityTypeAndActionType(EntityType entityType, AuditAction actionType, Pageable pageable);

    // Find logs by actor name and entity type
    Page<AuditLog> findByActorNameAndEntityType(String actorName, EntityType entityType, Pageable pageable);

    // Find recent logs (last N days)
    @Query("{ 'timestamp': { $gte: ?0 } }")
    List<AuditLog> findRecentLogs(LocalDateTime cutoffDate);

    // Count logs by entity type
    long countByEntityType(EntityType entityType);

    // Count logs by action type
    long countByActionType(AuditAction actionType);

    // Count logs by actor
    long countByActorName(String actorName);

    // Find logs with complex criteria
    @Query("{ $and: [ " +
            "{ $or: [ { 'entityType': { $exists: false } }, { 'entityType': ?0 } ] }, " +
            "{ $or: [ { 'actionType': { $exists: false } }, { 'actionType': ?1 } ] }, " +
            "{ $or: [ { 'actorName': { $exists: false } }, { 'actorName': ?2 } ] }, " +
            "{ 'timestamp': { $gte: ?3, $lte: ?4 } } " +
            "] }")
    Page<AuditLog> findLogsByCriteria(EntityType entityType, AuditAction actionType, String actorName,
                                      LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    // Delete old logs (cleanup)
    long deleteByTimestampBefore(LocalDateTime cutoffDate);
}