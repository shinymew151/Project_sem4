package com.ai.video.FacelessVideo.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;
import java.util.List;

@Entity
@Data
@Table(name = "Video")
public class Video {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private String id;
    
    @ManyToOne
    @JoinColumn(name = "audio_id")
    private TTSAudio audio;
    
    @Column(name = "video_path")
    private String videoPath;
    
    @Column(name = "lipsync_model")
    private String lipsyncModel;
    
    private String status;
    
    @OneToMany(mappedBy = "video", cascade = CascadeType.ALL)
    private List<Post> posts;
    
    @PrePersist
    protected void onCreate() {
        status = "GENERATED";
        lipsyncModel = "gooey-lipsync";
    }
}