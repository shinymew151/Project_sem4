package com.ai.video.FacelessVideo.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "Post")
public class Post {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private String id;
    
    @ManyToOne
    @JoinColumn(name = "video_id")
    private Video video;
    
    @ManyToOne
    @JoinColumn(name = "article_id")
    private Article article;
    
    private String platform;
    
    @Column(name = "post_url")
    private String postUrl;
    
    private String status;
    
    @Column(name = "posted_at")
    private LocalDateTime postedAt;
    
    @PrePersist
    protected void onCreate() {
        status = "PENDING";
    }
}
