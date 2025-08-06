package com.ai.video.FacelessVideo.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Data
@Table(name = "ScheduledTask")
public class ScheduledTask {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private String id;
    
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    
    @Column(name = "task_type")
    private String taskType;
    
    private String frequency;
    
    @Column(name = "start_time")
    private LocalTime startTime;
    
    @Column(name = "last_run_at")
    private LocalDateTime lastRunAt;
    
    private String status;
    
    @PrePersist
    protected void onCreate() {
        status = "ACTIVE";
    }
}
