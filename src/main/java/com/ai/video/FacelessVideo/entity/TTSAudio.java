package com.ai.video.FacelessVideo.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;
import java.util.List;

@Entity
@Data
@Table(name = "TTSAudio")
public class TTSAudio {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private String id;
    
    @ManyToOne
    @JoinColumn(name = "article_id")
    private Article article;
    
    @Column(name = "audio_path")
    private String audioPath;
    
    @Column(name = "voice_model")
    private String voiceModel;
    
    private String status;
    
    @OneToMany(mappedBy = "audio", cascade = CascadeType.ALL)
    private List<Video> videos;
    
    @PrePersist
    protected void onCreate() {
        status = "GENERATED";
        voiceModel = "vi-VN-Neural2-D"; // Default male Vietnamese voice
    }
}
