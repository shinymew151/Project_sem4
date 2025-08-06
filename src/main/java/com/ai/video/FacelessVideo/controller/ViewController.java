package com.ai.video.FacelessVideo.controller;

import com.ai.video.FacelessVideo.repository.*;
import com.ai.video.FacelessVideo.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
public class ViewController {

    private static final Logger logger = LoggerFactory.getLogger(ViewController.class);

    @Autowired
    private ArticleRepository articleRepository;
    
    @Autowired
    private VideoRepository videoRepository;
    
    @Autowired
    private TTSAudioRepository ttsAudioRepository;
    
    @Autowired
    private ProcessingLogRepository processingLogRepository;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        try {
            // Get recent articles with their associated videos
            List<Article> articles = articleRepository.findAllByOrderByCreatedAtDesc();
            
            // Get all videos ordered by article creation date
            List<Video> videos = videoRepository.findAllOrderByArticleCreatedAt();
            
            // Get recent processing logs for debugging
            List<ProcessingLog> recentLogs = processingLogRepository.findAll()
                    .stream()
                    .sorted((log1, log2) -> log2.getTimestamp().compareTo(log1.getTimestamp()))
                    .limit(10)
                    .collect(Collectors.toList());
            
            // Create video records for dashboard display (maintaining compatibility)
            List<VideoRecord> videoRecords = videos.stream()
                    .filter(video -> video.getAudio() != null && video.getAudio().getArticle() != null)
                    .map(video -> {
                        VideoRecord record = new VideoRecord();
                        record.setId(Long.valueOf(video.hashCode())); // Temporary ID for display
                        
                        Article article = video.getAudio().getArticle();
                        String text = article.getGeneratedText();
                        if (text != null && text.length() > 200) {
                            text = text.substring(0, 200) + "...";
                        }
                        record.setText(text != null ? text : "No content available");
                        
                        record.setAudioPath(video.getAudio().getAudioPath());
                        record.setVideoPath(video.getVideoPath());
                        
                        return record;
                    })
                    .collect(Collectors.toList());

            model.addAttribute("records", videoRecords);
            model.addAttribute("articles", articles);
            model.addAttribute("recentLogs", recentLogs);
            model.addAttribute("totalArticles", articles.size());
            model.addAttribute("totalVideos", videos.size());
            
            logger.info("Dashboard loaded with {} articles and {} videos", articles.size(), videos.size());
            
        } catch (Exception e) {
            logger.error("Error loading dashboard data", e);
            model.addAttribute("records", Collections.emptyList());
            model.addAttribute("articles", Collections.emptyList());
            model.addAttribute("recentLogs", Collections.emptyList());
            model.addAttribute("totalArticles", 0);
            model.addAttribute("totalVideos", 0);
            model.addAttribute("errorMessage", "Error loading dashboard data: " + e.getMessage());
        }

        return "dashboard";
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/dashboard";
    }
    
    @GetMapping("/articles")
    public String articles(Model model) {
        try {
            List<Article> articles = articleRepository.findAllByOrderByCreatedAtDesc();
            model.addAttribute("articles", articles);
            
            // Add statistics
            long totalArticles = articleRepository.count();
            long generatedArticles = articleRepository.findByStatus("GENERATED").size();
            long completedArticles = articleRepository.findByStatus("COMPLETED").size();
            
            model.addAttribute("totalArticles", totalArticles);
            model.addAttribute("generatedArticles", generatedArticles);
            model.addAttribute("completedArticles", completedArticles);
            
        } catch (Exception e) {
            logger.error("Error loading articles", e);
            model.addAttribute("articles", Collections.emptyList());
            model.addAttribute("errorMessage", "Error loading articles: " + e.getMessage());
        }
        
        return "articles";
    }
    
    @GetMapping("/videos")
    public String videos(Model model) {
        try {
            List<Video> videos = videoRepository.findAllOrderByArticleCreatedAt();
            model.addAttribute("videos", videos);
            
            // Add statistics
            long totalVideos = videoRepository.count();
            long generatedVideos = videoRepository.findByStatus("GENERATED").size();
            long completedVideos = videoRepository.findByStatus("COMPLETED").size();
            
            model.addAttribute("totalVideos", totalVideos);
            model.addAttribute("generatedVideos", generatedVideos);
            model.addAttribute("completedVideos", completedVideos);
            
        } catch (Exception e) {
            logger.error("Error loading videos", e);
            model.addAttribute("videos", Collections.emptyList());
            model.addAttribute("errorMessage", "Error loading videos: " + e.getMessage());
        }
        
        return "videos";
    }
    
    @GetMapping("/processing-logs")
    public String processingLogs(Model model) {
        try {
            List<ProcessingLog> logs = processingLogRepository.findAll()
                    .stream()
                    .sorted((log1, log2) -> log2.getTimestamp().compareTo(log1.getTimestamp()))
                    .limit(100) // Show last 100 logs
                    .collect(Collectors.toList());
            
            model.addAttribute("logs", logs);
            
            // Add statistics by stage
            long crawlLogs = logs.stream().filter(log -> "HEADLINE_CRAWL".equals(log.getStage())).count();
            long articleLogs = logs.stream().filter(log -> "ARTICLE_GENERATION".equals(log.getStage())).count();
            long videoLogs = logs.stream().filter(log -> "VIDEO_GENERATION".equals(log.getStage())).count();
            long mergeLogs = logs.stream().filter(log -> "VIDEO_MERGE".equals(log.getStage())).count();
            
            model.addAttribute("crawlLogs", crawlLogs);
            model.addAttribute("articleLogs", articleLogs);
            model.addAttribute("videoLogs", videoLogs);
            model.addAttribute("mergeLogs", mergeLogs);
            
        } catch (Exception e) {
            logger.error("Error loading processing logs", e);
            model.addAttribute("logs", Collections.emptyList());
            model.addAttribute("errorMessage", "Error loading processing logs: " + e.getMessage());
        }
        
        return "processing-logs";
    }
}