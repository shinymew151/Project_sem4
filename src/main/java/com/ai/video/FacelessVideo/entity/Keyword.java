package com.ai.video.FacelessVideo.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;
import java.util.List;

@Entity
@Data
@Table(name = "Keyword")
public class Keyword {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private String id;
    
    private String keyword;
    private Float score;
    
    @ManyToOne
    @JoinColumn(name = "source_content_id")
    private SourceContent sourceContent;
    
    @OneToMany(mappedBy = "keyword", cascade = CascadeType.ALL)
    private List<Article> articles;
}
