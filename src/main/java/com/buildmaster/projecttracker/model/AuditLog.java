package com.buildmaster.projecttracker.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Map;

@Document(collection = "audit_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {
    
    @Id
    private String id;
    private AuditAction actionType; // CREATE, UPDATE, DELETE
    
    private EntityType entityType; // PROJECT, TASK, DEVELOPER
    
    private Long entityId;
    
    private LocalDateTime timestamp;
    
    private String actorName;
    
    private Map<String, Object> payload; // JSON representation of the data
    
    // Constructor for easy creation
    public AuditLog(AuditAction actionType, EntityType entityType, Long entityId,
                   String actorName, Map<String, Object> payload) {
        this.actionType = actionType;
        this.entityType = entityType;
        this.entityId = entityId;
        this.actorName = actorName;
        this.payload = payload;
        this.timestamp = LocalDateTime.now();
    }
}

