package com.ai.video.FacelessVideo.service;

import com.ai.video.FacelessVideo.entity.*;
import com.ai.video.FacelessVideo.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class WorkflowService {

    @Autowired
    private SourceContentRepository sourceContentRepository;
    
    @Autowired
    private KeywordRepository keywordRepository;
    
    @Autowired
    private ArticleRepository articleRepository;
    
    @Autowired
    private TTSAudioRepository ttsAudioRepository;
    
    @Autowired
    private VideoRepository videoRepository;
    
    @Autowired
    private ProcessingLogRepository processingLogRepository;
    
    @Value("${app.workflow.video-parts-count:5}")
    private int videoPartsCount;
    
    @Value("${app.workflow.target-duration-seconds:30}")
    private int targetDurationSeconds;
    
    public WorkflowStatus getWorkflowStatus(String sourceContentId) {
        WorkflowStatus status = new WorkflowStatus();
        
        if (sourceContentId != null) {
            Optional<SourceContent> sourceContent = sourceContentRepository.findById(sourceContentId);
            if (sourceContent.isPresent()) {
                status.setSourceContent(sourceContent.get());
                
                // Get keywords for this source content
                List<Keyword> keywords = keywordRepository.findBySourceContentId(sourceContentId);
                status.setKeywords(keywords);
                
                // Get articles for these keywords
                List<Article> articles = keywords.stream()
                        .flatMap(keyword -> keyword.getArticles().stream())
                        .toList();
                status.setArticles(articles);
                
                // Get TTS audios and videos
                List<TTSAudio> ttsAudios = articles.stream()
                        .flatMap(article -> article.getTtsAudios().stream())
                        .toList();
                status.setTtsAudios(ttsAudios);
                
                List<Video> videos = ttsAudios.stream()
                        .flatMap(audio -> audio.getVideos().stream())
                        .toList();
                status.setVideos(videos);
                
                // Get processing logs
                List<ProcessingLog> logs = processingLogRepository.findByRelatedIdOrderByTimestamp(sourceContentId);
                status.setProcessingLogs(logs);
            }
        }
        
        return status;
    }
    
    public static class WorkflowStatus {
        private SourceContent sourceContent;
        private List<Keyword> keywords;
        private List<Article> articles;
        private List<TTSAudio> ttsAudios;
        private List<Video> videos;
        private List<ProcessingLog> processingLogs;
        
        // Getters and setters
        public SourceContent getSourceContent() { return sourceContent; }
        public void setSourceContent(SourceContent sourceContent) { this.sourceContent = sourceContent; }
        
        public List<Keyword> getKeywords() { return keywords; }
        public void setKeywords(List<Keyword> keywords) { this.keywords = keywords; }
        
        public List<Article> getArticles() { return articles; }
        public void setArticles(List<Article> articles) { this.articles = articles; }
        
        public List<TTSAudio> getTtsAudios() { return ttsAudios; }
        public void setTtsAudios(List<TTSAudio> ttsAudios) { this.ttsAudios = ttsAudios; }
        
        public List<Video> getVideos() { return videos; }
        public void setVideos(List<Video> videos) { this.videos = videos; }
        
        public List<ProcessingLog> getProcessingLogs() { return processingLogs; }
        public void setProcessingLogs(List<ProcessingLog> processingLogs) { this.processingLogs = processingLogs; }
        
        public boolean isReadyForArticleGeneration() {
            return sourceContent != null && keywords != null && !keywords.isEmpty();
        }
        
        public boolean isReadyForVideoGeneration() {
            return articles != null && !articles.isEmpty() && 
                   articles.stream().anyMatch(a -> "GENERATED".equals(a.getStatus()));
        }
        
        public boolean isReadyForMerging() {
            return videos != null && videos.size() >= 3; // At least 3 video parts
        }
        
        public boolean isCompleted() {
            return videos != null && videos.stream().anyMatch(v -> "COMPLETED".equals(v.getStatus()));
        }
    }
}