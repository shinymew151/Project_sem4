package com.ai.video.FacelessVideo.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "video_record")
public class VideoRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(columnDefinition = "TEXT")
    private String text;
    
    @Column(name = "audio_path")
    private String audioPath;
    
    @Column(name = "video_path")
    private String videoPath;
}