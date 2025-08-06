package com.ai.video.FacelessVideo.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@Table(name = "Article")
public class Article {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private String id;
    
    @ManyToOne
    @JoinColumn(name = "keyword_id")
    private Keyword keyword;
    
    @ManyToOne
    @JoinColumn(name = "prompt_template_id")
    private PromptTemplate promptTemplate;
    
    @Column(name = "generated_text", columnDefinition = "TEXT")
    private String generatedText;
    
    private String status;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @ManyToOne
    @JoinColumn(name = "approved_by")
    private User approvedBy;
    
    @OneToMany(mappedBy = "article", cascade = CascadeType.ALL)
    private List<TTSAudio> ttsAudios;
    
    @OneToMany(mappedBy = "article", cascade = CascadeType.ALL)
    private List<Post> posts;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        status = "GENERATED";
    }
}