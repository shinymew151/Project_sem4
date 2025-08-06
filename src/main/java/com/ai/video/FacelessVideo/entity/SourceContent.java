package com.ai.video.FacelessVideo.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@Table(name = "SourceContent")
public class SourceContent {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private String id;
    
    private String url;
    private String platform;
    
    @Column(name = "fetched_at")
    private LocalDateTime fetchedAt;
    
    private String status;
    private String hash;
    
    @Column(name = "raw_path")
    private String rawPath;
    
    @OneToMany(mappedBy = "sourceContent", cascade = CascadeType.ALL)
    private List<Keyword> keywords;
    
    @PrePersist
    protected void onCreate() {
        fetchedAt = LocalDateTime.now();
        status = "FETCHED";
    }
}
