package com.ai.video.FacelessVideo.controller;

import com.ai.video.FacelessVideo.entity.*;
import com.ai.video.FacelessVideo.repository.*;
import com.ai.video.FacelessVideo.service.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Controller
public class CrawlController {

    @Autowired
    private ChatGPTService chatGPTService;

    @Autowired
    private GoogleTTSService ttsService;

    @Autowired
    private GooeyLipSyncService gooeyLipSyncService;

    @Autowired
    private VideoProcessingService videoProcessingService;

    // New repositories for the updated schema
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
    private PromptTemplateRepository promptTemplateRepository;
    
    @Autowired
    private ProcessingLogRepository processingLogRepository;
    
    @Autowired
    private AuditLogRepository auditLogRepository;

    @Value("${server.base.url}")
    private String serverBaseUrl;

    // Thread pool for parallel processing (increased for 5 videos)
    private final ExecutorService executorService = Executors.newFixedThreadPool(5);

    // Store current workflow state in memory (in production, use Redis or database)
    private static String currentSourceContentId = null;
    private static String currentKeywordId = null;
    private static String currentArticleId = null;
    private static List<String> currentTTSAudioIds = new ArrayList<>();
    private static List<String> currentVideoIds = new ArrayList<>();

    /**
     * Step 1: Get headline from VNExpress and store as SourceContent
     */
    @GetMapping("/get-headline")
    @ResponseBody
    public ResponseEntity<Map<String, String>> getHeadline() {
        try {
            System.out.println("📰 Step 1: Crawling VNExpress for headline...");
            
            // Log the start of processing
            logProcessing("HEADLINE_CRAWL", null, "STARTED", "Starting VNExpress headline crawl");
            
            Document doc = Jsoup.connect("https://vnexpress.net/").get();
            Element headlineElement = doc.selectFirst("h3.title-news a, h4.title-news a");

            if (headlineElement == null) {
                logProcessing("HEADLINE_CRAWL", null, "FAILED", "Could not find headline on VNExpress");
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Could not find a headline on VNExpress. Please try again later."));
            }

            String headline = headlineElement.text();
            String url = headlineElement.attr("abs:href");
            
            // Create hash for deduplication
            String hash = generateHash(headline);
            
            // Check if this content already exists
            if (sourceContentRepository.findByHash(hash).isPresent()) {
                System.out.println("⚠️ Headline already exists in database");
                return ResponseEntity.ok(Map.of(
                    "headline", headline,
                    "status", "duplicate",
                    "message", "This headline has already been processed"
                ));
            }
            
            // Create and save SourceContent
            SourceContent sourceContent = new SourceContent();
            sourceContent.setUrl(url);
            sourceContent.setPlatform("vnexpress");
            sourceContent.setHash(hash);
            sourceContent.setRawPath(headline); // Store headline in rawPath for now
            sourceContent = sourceContentRepository.save(sourceContent);
            
            // Create and save Keyword (using headline as keyword)
            Keyword keyword = new Keyword();
            keyword.setKeyword(headline);
            keyword.setScore(1.0f); // Default high score for headlines
            keyword.setSourceContent(sourceContent);
            keyword = keywordRepository.save(keyword);
            
            // Store IDs for next steps
            currentSourceContentId = sourceContent.getId();
            currentKeywordId = keyword.getId();
            
            System.out.println("✅ Found headline: " + headline);
            System.out.println("💾 Saved as SourceContent ID: " + currentSourceContentId);
            
            logProcessing("HEADLINE_CRAWL", currentSourceContentId, "COMPLETED", "Headline crawled and saved successfully");

            return ResponseEntity.ok(Map.of(
                "headline", headline,
                "sourceContentId", currentSourceContentId,
                "keywordId", currentKeywordId,
                "status", "success",
                "message", "Headline retrieved and saved successfully"
            ));

        } catch (Exception e) {
            System.err.println("❌ Failed to get headline: " + e.getMessage());
            logProcessing("HEADLINE_CRAWL", null, "FAILED", "Failed to get headline: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to get headline: " + e.getMessage()));
        }
    }

    /**
     * Step 2: Generate Vietnamese article from keyword and store as Article
     */
    @PostMapping("/generate-article")
    @ResponseBody
    public ResponseEntity<Map<String, String>> generateArticle(@RequestBody Map<String, String> body) {
        try {
            String headline = body.get("headline");
            if (headline == null || headline.trim().isEmpty()) {
                // Try to get from current keyword
                if (currentKeywordId != null) {
                    Keyword keyword = keywordRepository.findById(currentKeywordId).orElse(null);
                    if (keyword != null) {
                        headline = keyword.getKeyword();
                    }
                }
                
                if (headline == null) {
                    return ResponseEntity.badRequest()
                            .body(Map.of("error", "No headline provided and no current keyword available"));
                }
            }

            System.out.println("📝 Step 2: Generating Vietnamese article from headline (optimized for 5 parts)...");
            logProcessing("ARTICLE_GENERATION", currentKeywordId, "STARTED", "Starting article generation");
            
            // Get default prompt template for Vietnamese articles
            PromptTemplate promptTemplate = getOrCreateDefaultPromptTemplate();
            
            // Generate article using ChatGPT service
            String generatedText = chatGPTService.generateVietnameseArticleFromHeadline(headline);
            
            // Create and save Article
            Article article = new Article();
            if (currentKeywordId != null) {
                Keyword keyword = keywordRepository.findById(currentKeywordId).orElse(null);
                article.setKeyword(keyword);
            }
            article.setPromptTemplate(promptTemplate);
            article.setGeneratedText(generatedText);
            article.setStatus("GENERATED");
            article = articleRepository.save(article);
            
            currentArticleId = article.getId();
            
            System.out.println("✅ Vietnamese article generated (" + generatedText.length() + " characters)");
            System.out.println("💾 Saved as Article ID: " + currentArticleId);
            
            logProcessing("ARTICLE_GENERATION", currentArticleId, "COMPLETED", "Article generated and saved successfully");

            return ResponseEntity.ok(Map.of(
                "article", generatedText,
                "articleId", currentArticleId,
                "headline", headline,
                "status", "success",
                "message", "Vietnamese article generated and saved successfully (optimized for 5 video parts)"
            ));

        } catch (Exception e) {
            System.err.println("❌ Failed to generate article: " + e.getMessage());
            logProcessing("ARTICLE_GENERATION", currentArticleId, "FAILED", "Failed to generate article: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to generate article: " + e.getMessage()));
        }
    }

    /**
     * Step 3: Generate all FIVE video parts with full database tracking
     */
    @PostMapping("/generate-videos")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> generateVideos(@RequestBody Map<String, String> body) {
        try {
            String articleText = body.get("article");
            Article article = null;
            
            if (articleText == null || articleText.trim().isEmpty()) {
                if (currentArticleId != null) {
                    article = articleRepository.findById(currentArticleId).orElse(null);
                    if (article != null) {
                        articleText = article.getGeneratedText();
                    }
                }
                
                if (articleText == null) {
                    return ResponseEntity.badRequest()
                            .body(Map.of("error", "No article provided and no current article available"));
                }
            } else if (currentArticleId != null) {
                article = articleRepository.findById(currentArticleId).orElse(null);
            }

            System.out.println("🎬 Step 3: Generating FIVE video parts with database tracking...");
            logProcessing("VIDEO_GENERATION", currentArticleId, "STARTED", "Starting 5-part video generation");

            // Step 3.1: Split into 5 parts
            System.out.println("✂️ Splitting article into 5 parts...");
            String[] articleParts = ttsService.splitTextIntoFiveParts(articleText);
            System.out.println("✅ Article split into " + articleParts.length + " parts");

            // Step 3.2: Generate Vietnamese TTS for each part and save to database
            System.out.println("🎵 Generating Vietnamese MALE TTS for all 5 parts...");
            List<CompletableFuture<TTSAudio>> audioFutures = new ArrayList<>();
            
            for (int i = 0; i < articleParts.length; i++) {
                final int partIndex = i;
                final String partText = articleParts[i];
                final Article finalArticle = article;
                
                CompletableFuture<TTSAudio> audioFuture = CompletableFuture.supplyAsync(() -> {
                    try {
                        System.out.println("🎤 Generating Vietnamese MALE TTS for part " + (partIndex + 1) + "/5");
                        String audioFileName = ttsService.generateVietnameseSpeechForPart(partText, partIndex + 1);
                        
                        // Create and save TTSAudio record
                        TTSAudio ttsAudio = new TTSAudio();
                        ttsAudio.setArticle(finalArticle);
                        ttsAudio.setAudioPath(audioFileName);
                        ttsAudio.setVoiceModel("vi-VN-Neural2-D");
                        ttsAudio.setStatus("GENERATED");
                        return ttsAudioRepository.save(ttsAudio);
                        
                    } catch (Exception e) {
                        System.err.println("❌ TTS failed for part " + (partIndex + 1) + ": " + e.getMessage());
                        throw new RuntimeException("TTS failed for part " + (partIndex + 1), e);
                    }
                }, executorService);
                
                audioFutures.add(audioFuture);
            }

            // Wait for all TTS to complete
            List<TTSAudio> ttsAudios = new ArrayList<>();
            for (int i = 0; i < audioFutures.size(); i++) {
                try {
                    TTSAudio ttsAudio = audioFutures.get(i).get();
                    ttsAudios.add(ttsAudio);
                    currentTTSAudioIds.add(ttsAudio.getId());
                    System.out.println("✅ TTS completed for part " + (i + 1) + "/5: " + ttsAudio.getAudioPath());
                } catch (Exception e) {
                    throw new RuntimeException("TTS failed for part " + (i + 1) + ": " + e.getMessage());
                }
            }

            // Step 3.3: Generate lip-sync videos for each part and save to database
            System.out.println("🎥 Generating lip-sync videos for all 5 parts...");
            List<CompletableFuture<Video>> videoFutures = new ArrayList<>();
            
            for (int i = 0; i < ttsAudios.size(); i++) {
                final int partIndex = i;
                final TTSAudio ttsAudio = ttsAudios.get(i);
                
                CompletableFuture<Video> videoFuture = CompletableFuture.supplyAsync(() -> {
                    try {
                        System.out.println("🎥 Generating video for part " + (partIndex + 1) + "/5");
                        String audioUrl = serverBaseUrl + "/output/" + ttsAudio.getAudioPath();
                        String imagePath = "assets/avatar.jpg";
                        String videoUrl = gooeyLipSyncService.generateVideoFromGooey(audioUrl, imagePath);
                        
                        // Download the video
                        String videoFileName = "video_part_" + (partIndex + 1) + "_" + java.util.UUID.randomUUID() + ".mp4";
                        String localVideoPath = videoProcessingService.downloadVideo(videoUrl, videoFileName);
                        
                        // Create and save Video record
                        Video video = new Video();
                        video.setAudio(ttsAudio);
                        video.setVideoPath(localVideoPath);
                        video.setLipsyncModel("gooey-lipsync");
                        video.setStatus("GENERATED");
                        return videoRepository.save(video);
                        
                    } catch (Exception e) {
                        System.err.println("❌ Video generation failed for part " + (partIndex + 1) + ": " + e.getMessage());
                        throw new RuntimeException("Video generation failed for part " + (partIndex + 1), e);
                    }
                }, executorService);
                
                videoFutures.add(videoFuture);
            }

            // Wait for all videos to complete and collect successful ones
            List<Video> successfulVideos = new ArrayList<>();
            List<String> failedParts = new ArrayList<>();
            
            for (int i = 0; i < videoFutures.size(); i++) {
                try {
                    Video video = videoFutures.get(i).get();
                    successfulVideos.add(video);
                    currentVideoIds.add(video.getId());
                    System.out.println("✅ Video completed for part " + (i + 1) + "/5: " + video.getVideoPath());
                } catch (Exception e) {
                    String partName = "part " + (i + 1);
                    failedParts.add(partName);
                    System.err.println("❌ Video generation failed for " + partName + ": " + e.getMessage());
                }
            }
            
            if (successfulVideos.isEmpty()) {
                logProcessing("VIDEO_GENERATION", currentArticleId, "FAILED", "All video parts failed to generate");
                throw new RuntimeException("All video parts failed to generate. Please try again later.");
            }

            // Extract video paths for response
            List<String> videoPaths = successfulVideos.stream()
                    .map(Video::getVideoPath)
                    .toList();

            logProcessing("VIDEO_GENERATION", currentArticleId, "COMPLETED", 
                "Video generation completed: " + successfulVideos.size() + "/5 parts successful");

            return ResponseEntity.ok(Map.of(
                "videoPaths", videoPaths,
                "videoIds", currentVideoIds,
                "ttsAudioIds", currentTTSAudioIds,
                "successCount", successfulVideos.size(),
                "totalParts", articleParts.length,
                "failedParts", failedParts,
                "status", "success",
                "message", "Videos generated and saved successfully: " + successfulVideos.size() + "/5 parts (MALE VOICE)"
            ));

        } catch (Exception e) {
            System.err.println("❌ Failed to generate videos: " + e.getMessage());
            logProcessing("VIDEO_GENERATION", currentArticleId, "FAILED", "Failed to generate videos: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to generate videos: " + e.getMessage()));
        }
    }

    /**
     * Step 4: Merge videos and update database records
     */
    @PostMapping("/merge-and-save")
    @ResponseBody
    public ResponseEntity<Map<String, String>> mergeAndSave() {
        try {
            if (currentVideoIds.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "No video parts available to merge"));
            }

            System.out.println("🔗 Step 4: Merging all " + currentVideoIds.size() + " videos...");
            logProcessing("VIDEO_MERGE", currentArticleId, "STARTED", "Starting video merge process");
            
            // Get video paths from database
            List<String> videoPaths = new ArrayList<>();
            for (String videoId : currentVideoIds) {
                Video video = videoRepository.findById(videoId).orElse(null);
                if (video != null) {
                    videoPaths.add(video.getVideoPath());
                }
            }
            
            String finalVideoPath = videoProcessingService.mergeVideos(videoPaths);
            System.out.println("✅ Videos merged successfully: " + finalVideoPath);

            // Update article status
            if (currentArticleId != null) {
                Article article = articleRepository.findById(currentArticleId).orElse(null);
                if (article != null) {
                    article.setStatus("COMPLETED");
                    articleRepository.save(article);
                }
            }

            // Create a final merged video record
            Video mergedVideo = new Video();
            mergedVideo.setVideoPath(finalVideoPath);
            mergedVideo.setLipsyncModel("merged-gooey-lipsync");
            mergedVideo.setStatus("COMPLETED");
            Video savedMergedVideo = videoRepository.save(mergedVideo);

            // Clear current state
            currentSourceContentId = null;
            currentKeywordId = null;
            currentArticleId = null;
            currentTTSAudioIds.clear();
            currentVideoIds.clear();

            logProcessing("VIDEO_MERGE", savedMergedVideo.getId(), "COMPLETED", "Video merge completed successfully");
            System.out.println("🎉 Process completed successfully! Final video should be ~30 seconds with better coverage.");

            return ResponseEntity.ok(Map.of(
                "finalVideoPath", finalVideoPath,
                "mergedVideoId", savedMergedVideo.getId(),
                "status", "success",
                "message", "Video merged and saved successfully! (5 parts, MALE VOICE, ~30 seconds)",
                "videoUrl", "/" + finalVideoPath
            ));

        } catch (Exception e) {
            System.err.println("❌ Failed to merge and save: " + e.getMessage());
            logProcessing("VIDEO_MERGE", currentArticleId, "FAILED", "Failed to merge videos: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to merge and save: " + e.getMessage()));
        }
    }

    /**
     * Get current workflow status with database state
     */
    @GetMapping("/workflow-status")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getWorkflowStatus() {
        Map<String, Object> statusMap = new HashMap<>();
        statusMap.put("currentSourceContentId", currentSourceContentId != null ? currentSourceContentId : "");
        statusMap.put("currentKeywordId", currentKeywordId != null ? currentKeywordId : "");
        statusMap.put("currentArticleId", currentArticleId != null ? currentArticleId : "");
        statusMap.put("hasSourceContent", currentSourceContentId != null);
        statusMap.put("hasKeyword", currentKeywordId != null);
        statusMap.put("hasArticle", currentArticleId != null);
        statusMap.put("videoParts", currentVideoIds.size());
        statusMap.put("audioParts", currentTTSAudioIds.size());
        statusMap.put("totalPartsExpected", 5);
        statusMap.put("canProceedToGenerate", currentKeywordId != null);
        statusMap.put("canProceedToVideos", currentArticleId != null);
        statusMap.put("canProceedToMerge", !currentVideoIds.isEmpty());
        
        return ResponseEntity.ok(statusMap);
    }

    /**
     * Reset workflow state
     */
    @PostMapping("/reset-workflow")
    @ResponseBody
    public ResponseEntity<Map<String, String>> resetWorkflow() {
        currentSourceContentId = null;
        currentKeywordId = null;
        currentArticleId = null;
        currentTTSAudioIds.clear();
        currentVideoIds.clear();
        
        return ResponseEntity.ok(Map.of(
            "status", "success",
            "message", "Workflow state reset successfully"
        ));
    }

    @GetMapping("/test-ffmpeg")
    public ResponseEntity<String> testFFmpeg() {
        boolean ffmpegAvailable = videoProcessingService.isFFmpegAvailable();
        if (ffmpegAvailable) {
            return ResponseEntity.ok("✅ FFmpeg is available and working");
        } else {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body("❌ FFmpeg is not available. Please install FFmpeg to merge videos.");
        }
    }

    // Helper methods
    
    private String generateHash(String content) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(content.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            return String.valueOf(content.hashCode());
        }
    }
    
    private PromptTemplate getOrCreateDefaultPromptTemplate() {
        return promptTemplateRepository.findByCategoryAndActiveTrue("vietnamese-article")
                .stream()
                .findFirst()
                .orElseGet(() -> {
                    PromptTemplate template = new PromptTemplate();
                    template.setTitle("Default Vietnamese Article Template");
                    template.setCategory("vietnamese-article");
                    template.setTemplate("Generate a detailed Vietnamese article from the given headline, optimized for 5-part video generation");
                    template.setActive(true);
                    return promptTemplateRepository.save(template);
                });
    }
    
    private void logProcessing(String stage, String relatedId, String status, String message) {
        try {
            ProcessingLog log = new ProcessingLog();
            log.setStage(stage);
            log.setRelatedId(relatedId);
            log.setStatus(status);
            log.setMessage(message);
            processingLogRepository.save(log);
        } catch (Exception e) {
            System.err.println("Failed to log processing: " + e.getMessage());
        }
    }
}