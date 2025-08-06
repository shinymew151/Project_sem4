package com.ai.video.FacelessVideo.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "AuditLog")
public class AuditLog {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private String id;
    
    private String action;
    
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    
    @Column(name = "target_type")
    private String targetType;
    
    @Column(name = "target_id")
    private String targetId;
    
    private LocalDateTime timestamp;
    
    @Column(columnDefinition = "TEXT")
    private String details;
    
    @PrePersist
    protected void onCreate() {
        timestamp = LocalDateTime.now();
    }
}
