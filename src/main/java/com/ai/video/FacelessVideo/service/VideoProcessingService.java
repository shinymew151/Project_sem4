package com.ai.video.FacelessVideo.service;

import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
public class VideoProcessingService {

    /**
     * Merge multiple video files into one using FFmpeg
     * @param videoPaths List of video file paths to merge
     * @return Path to the merged video file
     */
    public String mergeVideos(List<String> videoPaths) {
        try {
            if (videoPaths == null || videoPaths.isEmpty()) {
                throw new RuntimeException("No video paths provided for merging");
            }

            String outputFileName = "merged_video_" + UUID.randomUUID() + ".mp4";
            String outputPath = "output/" + outputFileName;
            String listFileName = "video_list_" + UUID.randomUUID() + ".txt";
            
            // Create a temporary file list for FFmpeg concat
            createVideoListFile(videoPaths, listFileName);
            
            // FFmpeg command to concatenate videos
            String[] command = {
                "ffmpeg", "-y", // -y to overwrite output file
                "-f", "concat",
                "-safe", "0",
                "-i", listFileName,
                "-c", "copy", // Use stream copy for faster processing
                outputPath
            };

            System.out.println("🎬 Merging " + videoPaths.size() + " videos into: " + outputPath);
            
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();
            
            // Read output for debugging
            try (var reader = new java.io.BufferedReader(new java.io.InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println("FFmpeg: " + line);
                }
            }
            
            int exitCode = process.waitFor();
            
            // Clean up temporary list file
            Files.deleteIfExists(Paths.get(listFileName));
            
            if (exitCode != 0) {
                throw new RuntimeException("FFmpeg failed with exit code: " + exitCode);
            }
            
            // Verify output file exists
            File outputFile = new File(outputPath);
            if (!outputFile.exists() || outputFile.length() == 0) {
                throw new RuntimeException("Merged video file was not created or is empty");
            }
            
            System.out.println("✅ Videos merged successfully: " + outputPath + " (size: " + outputFile.length() + " bytes)");
            return outputPath;
            
        } catch (Exception e) {
            System.err.println("❌ Video merging failed: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Video merging failed: " + e.getMessage());
        }
    }

    /**
     * Create a file list for FFmpeg concat demuxer
     */
    private void createVideoListFile(List<String> videoPaths, String listFileName) throws IOException {
        try (FileWriter writer = new FileWriter(listFileName)) {
            for (String videoPath : videoPaths) {
                // Convert relative path to absolute path for FFmpeg
                File videoFile = new File(videoPath);
                String absolutePath = videoFile.getAbsolutePath();
                writer.write("file '" + absolutePath + "'\n");
            }
        }
        System.out.println("📝 Created video list file: " + listFileName);
    }

    /**
     * Download video from URL and save to local file
     */
    public String downloadVideo(String videoUrl, String fileName) {
        try {
            String outputPath = "output/" + fileName;
            
            System.out.println("⬇️ Downloading video from: " + videoUrl);
            System.out.println("💾 Saving to: " + outputPath);
            
            try (var inputStream = new java.net.URL(videoUrl).openStream();
                 var outputStream = new java.io.FileOutputStream(outputPath)) {
                inputStream.transferTo(outputStream);
            }
            
            File outputFile = new File(outputPath);
            if (!outputFile.exists() || outputFile.length() == 0) {
                throw new RuntimeException("Downloaded video file is empty or does not exist");
            }
            
            System.out.println("✅ Video downloaded successfully: " + outputPath + " (size: " + outputFile.length() + " bytes)");
            return outputPath;
            
        } catch (Exception e) {
            System.err.println("❌ Video download failed: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Video download failed: " + e.getMessage());
        }
    }

    /**
     * Check if FFmpeg is available on the system
     */
    public boolean isFFmpegAvailable() {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("ffmpeg", "-version");
            Process process = processBuilder.start();
            int exitCode = process.waitFor();
            return exitCode == 0;
        } catch (Exception e) {
            return false;
        }
    }
}