package com.ai.video.FacelessVideo.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.http.MediaType;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.client.MultipartBodyBuilder;

import java.io.File;
import java.nio.file.Paths;
import java.util.Map;
import java.util.HashMap;

@Service
public class GooeyLipSyncService {

    @Value("${gooey.api.key}")
    private String apiKey;

    @Value("${server.base.url:http://localhost:8080}")
    private String serverUrl;

    // Retry configuration
    private static final int MAX_RETRIES = 3;
    private static final long RETRY_DELAY_MS = 5000; // 5 seconds
    private static final long RATE_LIMIT_DELAY_MS = 10000; // 10 seconds

    public String generateVideoFromGooey(String audioPath, String imagePath) {
        return generateVideoWithRetry(audioPath, imagePath, 0);
    }

    private String generateVideoWithRetry(String audioPath, String imagePath, int attempt) {
        try {
            System.out.println("🎬 Attempt " + (attempt + 1) + "/" + (MAX_RETRIES + 1) + " for video generation");
            
            // Clean and validate file paths
            String audioFileName = cleanFileName(audioPath);
            String imageFileName = cleanFileName(imagePath);
            
            System.out.println("Audio filename extracted: " + audioFileName);
            System.out.println("Image filename extracted: " + imageFileName);

            // Build local file paths for existence check
            File audioFile = new File("output", audioFileName);
            File imageFile = new File("assets", imageFileName);

            System.out.println("Looking for audio at: " + audioFile.getAbsolutePath());
            System.out.println("Looking for image at: " + imageFile.getAbsolutePath());

            if (!audioFile.exists()) {
                throw new RuntimeException("Audio file does not exist: " + audioFile.getAbsolutePath());
            }
            
            if (!imageFile.exists()) {
                throw new RuntimeException("Image file does not exist: " + imageFile.getAbsolutePath());
            }

            // Try file upload approach first (recommended)
            return uploadFilesToGooeyWithRetry(audioFile, imageFile);

        } catch (Exception e) {
            if (isRateLimitError(e) && attempt < MAX_RETRIES) {
                System.err.println("⏳ Rate limit hit, waiting " + RATE_LIMIT_DELAY_MS/1000 + " seconds before retry...");
                try {
                    Thread.sleep(RATE_LIMIT_DELAY_MS);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Interrupted during rate limit wait", ie);
                }
                return generateVideoWithRetry(audioPath, imagePath, attempt + 1);
            } else if (attempt < MAX_RETRIES) {
                System.err.println("❌ Attempt " + (attempt + 1) + " failed, retrying in " + RETRY_DELAY_MS/1000 + " seconds...");
                try {
                    Thread.sleep(RETRY_DELAY_MS);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Interrupted during retry wait", ie);
                }
                return generateVideoWithRetry(audioPath, imagePath, attempt + 1);
            } else {
                System.err.println("💥 All attempts failed for video generation");
                throw new RuntimeException("Video generation failed after " + (MAX_RETRIES + 1) + " attempts. Last error: " + e.getMessage());
            }
        }
    }

    private boolean isRateLimitError(Exception e) {
        if (e instanceof WebClientResponseException) {
            WebClientResponseException webEx = (WebClientResponseException) e;
            return webEx.getStatusCode().value() == 429;
        }
        return e.getMessage() != null && e.getMessage().contains("429");
    }

    private String uploadFilesToGooeyWithRetry(File audioFile, File imageFile) {
        try {
            WebClient webClient = WebClient.builder()
                    .baseUrl("https://api.gooey.ai")
                    .defaultHeader("Authorization", "Bearer " + apiKey)
                    .build();

            // Create multipart body
            MultipartBodyBuilder bodyBuilder = new MultipartBodyBuilder();
            
            // Add JSON payload (can be empty object)
            bodyBuilder.part("json", "{}");
            
            // Add files
            bodyBuilder.part("input_face", new FileSystemResource(imageFile));
            bodyBuilder.part("input_audio", new FileSystemResource(audioFile));

            System.out.println("📤 Uploading files to Gooey.ai...");
            System.out.println("🎵 Audio file: " + audioFile.getName());
            System.out.println("🖼️ Image file: " + imageFile.getName());

            Map<String, Object> response = webClient.post()
                    .uri("/v2/Lipsync/form/")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .bodyValue(bodyBuilder.build())
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            System.out.println("✅ Gooey.ai file upload response received");
            System.out.println("📋 Response keys: " + (response != null ? response.keySet() : "null"));

            return extractVideoUrl(response);

        } catch (WebClientResponseException e) {
            System.err.println("❌ Gooey.ai file upload error - Status: " + e.getStatusCode());
            System.err.println("📄 Response body: " + e.getResponseBodyAsString());
            
            if (e.getStatusCode().value() == 429) {
                throw new RuntimeException("Rate limit exceeded: " + e.getMessage());
            }
            throw new RuntimeException("Gooey.ai file upload error: " + e.getMessage());
        }
    }

    private String sendUrlsToGooey(String audioPath, String imagePath) {
        try {
            // Clean and validate file paths
            String audioFileName = cleanFileName(audioPath);
            String imageFileName = cleanFileName(imagePath);

            // Construct public URLs (used by Gooey) - fix the URL construction
            String publicAudioUrl = buildPublicUrl(serverUrl, "output", audioFileName);
            String publicImageUrl = buildPublicUrl(serverUrl, "assets", imageFileName);
            
            System.out.println("🔗 Public audio URL: " + publicAudioUrl);
            System.out.println("🔗 Public image URL: " + publicImageUrl);

            WebClient webClient = WebClient.builder()
                    .baseUrl("https://api.gooey.ai")
                    .defaultHeader("Authorization", "Bearer " + apiKey)
                    .defaultHeader("Content-Type", "application/json")
                    .build();

            // Create request body with proper structure for URL endpoint
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("input_audio", publicAudioUrl);
            requestBody.put("input_face", publicImageUrl);

            System.out.println("📤 Sending URLs to Gooey.ai with body: " + requestBody);

            Map<String, Object> response = webClient.post()
                    .uri("/v2/Lipsync/")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            System.out.println("✅ Gooey.ai URL response received");

            return extractVideoUrl(response);

        } catch (WebClientResponseException e) {
            System.err.println("❌ Gooey.ai URL API error - Status: " + e.getStatusCode());
            System.err.println("📄 Response body: " + e.getResponseBodyAsString());
            
            if (e.getStatusCode().value() == 429) {
                throw new RuntimeException("Rate limit exceeded: " + e.getMessage());
            }
            throw new RuntimeException("Gooey.ai URL API error: " + e.getMessage());
        }
    }

    private String cleanFileName(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            throw new RuntimeException("File path is null or empty");
        }
        
        System.out.println("🧹 Original filePath input: '" + filePath + "'");
        
        String fileName = filePath;
        
        // If it's a full URL, extract just the filename
        if (fileName.startsWith("http://") || fileName.startsWith("https://")) {
            fileName = fileName.substring(fileName.lastIndexOf("/") + 1);
            System.out.println("🔗 Extracted from URL: '" + fileName + "'");
        }
        
        // If it's a file path, get just the filename
        if (fileName.contains("/") || fileName.contains("\\")) {
            fileName = Paths.get(fileName).getFileName().toString();
            System.out.println("📁 Extracted from path: '" + fileName + "'");
        }
        
        // Remove folder prefixes that might have been included in the filename
        if (fileName.startsWith("output/")) {
            fileName = fileName.substring("output/".length());
            System.out.println("🗂️ Removed 'output/' prefix: '" + fileName + "'");
        }
        if (fileName.startsWith("assets/")) {
            fileName = fileName.substring("assets/".length());
            System.out.println("🗂️ Removed 'assets/' prefix: '" + fileName + "'");
        }
        
        // Handle any remaining path separators
        while (fileName.contains("/")) {
            fileName = fileName.substring(fileName.lastIndexOf("/") + 1);
        }
        while (fileName.contains("\\")) {
            fileName = fileName.substring(fileName.lastIndexOf("\\") + 1);
        }
        
        System.out.println("✨ Final cleaned filename: '" + fileName + "' from original: '" + filePath + "'");
        return fileName;
    }

    private String buildPublicUrl(String baseUrl, String folder, String fileName) {
        // Ensure baseUrl doesn't end with slash
        String cleanBaseUrl = baseUrl;
        if (cleanBaseUrl.endsWith("/")) {
            cleanBaseUrl = cleanBaseUrl.substring(0, cleanBaseUrl.length() - 1);
        }
        
        // Build URL step by step to avoid issues
        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append(cleanBaseUrl);
        
        if (!cleanBaseUrl.endsWith("/")) {
            urlBuilder.append("/");
        }
        
        urlBuilder.append(folder);
        
        if (!folder.endsWith("/")) {
            urlBuilder.append("/");
        }
        
        urlBuilder.append(fileName);
        
        String finalUrl = urlBuilder.toString();
        
        // Replace any double slashes (except after protocol)
        finalUrl = finalUrl.replaceAll("(?<!:)//+", "/");
        
        System.out.println("🔗 Built URL: '" + finalUrl + "' from base: '" + baseUrl + "', folder: '" + folder + "', file: '" + fileName + "'");
        return finalUrl;
    }

    private String extractVideoUrl(Map<String, Object> response) {
        if (response == null) {
            throw new RuntimeException("Response is null");
        }

        // Check for output structure (common in Gooey.ai responses)
        if (response.containsKey("output")) {
            Object output = response.get("output");
            if (output instanceof Map) {
                Map<String, Object> outputMap = (Map<String, Object>) output;
                
                // Try different possible keys for video URL in output
                String[] outputKeys = {"output_video", "video_url", "result_url", "url", "video"};
                for (String key : outputKeys) {
                    if (outputMap.containsKey(key) && outputMap.get(key) instanceof String) {
                        String videoUrl = (String) outputMap.get(key);
                        System.out.println("🎬 Found video URL in output." + key + ": " + videoUrl);
                        return videoUrl;
                    }
                }
            }
        }
        
        // Try direct keys in response
        String[] directKeys = {"output_video", "video_url", "result_url", "url", "video"};
        for (String key : directKeys) {
            if (response.containsKey(key) && response.get(key) instanceof String) {
                String videoUrl = (String) response.get(key);
                System.out.println("🎬 Found video URL in " + key + ": " + videoUrl);
                return videoUrl;
            }
        }
        
        System.err.println("❌ Available response keys: " + response.keySet());
        throw new RuntimeException("No video URL found in response. Available keys: " + response.keySet());
    }
}