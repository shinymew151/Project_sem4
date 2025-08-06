package com.ai.video.FacelessVideo.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "ProcessingLog")
public class ProcessingLog {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private String id;
    
    private String stage;
    
    @Column(name = "related_id")
    private String relatedId;
    
    private String status;
    
    @Column(columnDefinition = "TEXT")
    private String message;
    
    private LocalDateTime timestamp;
    
    @PrePersist
    protected void onCreate() {
        timestamp = LocalDateTime.now();
    }
}
