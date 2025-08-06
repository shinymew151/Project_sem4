package com.ai.video.FacelessVideo.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import reactor.netty.http.client.HttpClient;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

@Service
public class GoogleTTSService {

    @Value("${google.tts.api.key}")
    private String apiKey;

    private final WebClient webClient;

    public GoogleTTSService() {
        this.webClient = WebClient.builder()
                .baseUrl("https://texttospeech.googleapis.com")
                .clientConnector(new ReactorClientHttpConnector(HttpClient.create()))
                .exchangeStrategies(ExchangeStrategies.builder()
                        .codecs(config -> config.defaultCodecs().maxInMemorySize(10 * 1024 * 1024))
                        .build())
                .build();
    }

    /**
     * Generate English speech (kept for backward compatibility) - Now using male voice
     */
    public String generateSpeech(String text) {
        return generateSpeechInternal(text, "audio_" + UUID.randomUUID() + ".mp3", "en-US", "en-US-Neural2-D");
    }

    /**
     * Generate English speech for specific part (kept for backward compatibility) - Now using male voice
     */
    public String generateSpeechForPart(String text, int partNumber) {
        return generateSpeechInternal(text, "audio_part_" + partNumber + "_" + UUID.randomUUID() + ".mp3", "en-US", "en-US-Neural2-D");
    }

    /**
     * Generate Vietnamese speech - Now using male voice
     */
    public String generateVietnameseSpeech(String text) {
        return generateSpeechInternal(text, "audio_vi_" + UUID.randomUUID() + ".mp3", "vi-VN", "vi-VN-Neural2-D");
    }

    /**
     * Generate Vietnamese speech for specific part - Now using male voice
     */
    public String generateVietnameseSpeechForPart(String text, int partNumber) {
        return generateSpeechInternal(text, "audio_vi_part_" + partNumber + "_" + UUID.randomUUID() + ".mp3", "vi-VN", "vi-VN-Neural2-D");
    }

    /**
     * Internal method to generate speech with configurable language and voice
     */
    private String generateSpeechInternal(String text, String filename, String languageCode, String voiceName) {
        try {
            String outputPath = "output/" + filename;
            
            System.out.println("🎵 Generating " + languageCode + " speech (MALE VOICE) for: " + text.substring(0, Math.min(50, text.length())) + "...");
            System.out.println("📁 Output path: " + outputPath);
            System.out.println("🎙️ Voice: " + voiceName + " (Male)");

            // Configure voice based on language - ALL MALE VOICES
            Map<String, Object> voiceConfig;
            Map<String, Object> audioConfig;
            
            if (languageCode.equals("vi-VN")) {
                voiceConfig = Map.of(
                    "languageCode", "vi-VN",
                    "name", "vi-VN-Neural2-D" // High-quality Vietnamese MALE voice
                );
                audioConfig = Map.of(
                    "audioEncoding", "MP3",
                    "speakingRate", 0.85, // Slightly slower for Vietnamese clarity
                    "pitch", -2.0, // Lower pitch for more masculine sound
                    "volumeGainDb", 1.0 // Slightly louder
                );
            } else {
                voiceConfig = Map.of(
                    "languageCode", "en-US",
                    "name", "en-US-Neural2-D" // High-quality English MALE voice
                );
                audioConfig = Map.of(
                    "audioEncoding", "MP3",
                    "speakingRate", 0.9,
                    "pitch", -1.5, // Lower pitch for more masculine sound
                    "volumeGainDb", 1.0
                );
            }

            Map<String, Object> requestBody = Map.of(
                "input", Map.of("text", text),
                "voice", voiceConfig,
                "audioConfig", audioConfig
            );

            Map<?, ?> response = webClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .path("/v1/text:synthesize")
                            .queryParam("key", apiKey)
                            .build())
                    .header("Content-Type", "application/json")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response == null) {
                throw new RuntimeException("No response from Google TTS API");
            }

            String base64Audio = (String) response.get("audioContent");
            if (base64Audio == null) {
                System.err.println("TTS Response: " + response);
                throw new RuntimeException("No audioContent in TTS response");
            }

            byte[] audioBytes = Base64.getDecoder().decode(base64Audio);

            Files.createDirectories(Paths.get("output"));
            Files.write(Paths.get(outputPath), audioBytes);

            System.out.println("✅ " + languageCode + " MALE speech generated successfully: " + outputPath + " (size: " + audioBytes.length + " bytes)");
            // Return just the filename, not the full path
            return filename;

        } catch (Exception e) {
            System.err.println("❌ " + languageCode + " MALE TTS failed: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(languageCode + " MALE TTS failed: " + e.getMessage());
        }
    }

    /**
     * Split text into 5 equal parts for better coverage (was 3 parts)
     * Each part should be suitable for ~6 seconds of speech (30 seconds total)
     * Optimized for Vietnamese text
     */
    public String[] splitTextIntoFiveParts(String text) {
        // For Vietnamese, we might want to split by sentences as well as words
        String[] sentences = text.split("[.!?]\\s*");
        
        if (sentences.length >= 5) {
            // If we have 5 or more sentences, distribute them evenly
            return splitBySentencesFive(sentences);
        } else {
            // If we have fewer sentences, split by words
            return splitByWordsFive(text);
        }
    }

    /**
     * Split by sentences for better Vietnamese flow (5 parts)
     */
    private String[] splitBySentencesFive(String[] sentences) {
        String[] parts = new String[5];
        int sentencesPerPart = Math.max(1, sentences.length / 5);
        int currentIndex = 0;
        
        for (int i = 0; i < 5; i++) {
            StringBuilder part = new StringBuilder();
            int sentencesInThisPart = (i == 4) ? (sentences.length - currentIndex) : sentencesPerPart;
            
            for (int j = 0; j < sentencesInThisPart && currentIndex < sentences.length; j++) {
                if (part.length() > 0) {
                    part.append(". ");
                }
                part.append(sentences[currentIndex++].trim());
            }
            
            // Add final punctuation if not present
            String partText = part.toString().trim();
            if (!partText.isEmpty() && !partText.endsWith(".") && !partText.endsWith("!") && !partText.endsWith("?")) {
                partText += ".";
            }
            
            parts[i] = partText;
        }
        
        return parts;
    }

    /**
     * Split by words (fallback method for 5 parts)
     */
    private String[] splitByWordsFive(String text) {
        String[] words = text.split("\\s+");
        int totalWords = words.length;
        int wordsPerPart = Math.max(1, totalWords / 5);
        
        String[] parts = new String[5];
        int currentIndex = 0;
        
        for (int i = 0; i < 5; i++) {
            StringBuilder part = new StringBuilder();
            int wordsInThisPart = (i == 4) ? (totalWords - currentIndex) : wordsPerPart;
            
            for (int j = 0; j < wordsInThisPart && currentIndex < totalWords; j++) {
                if (part.length() > 0) {
                    part.append(" ");
                }
                part.append(words[currentIndex++]);
            }
            
            parts[i] = part.toString();
        }
        
        // Log the split for debugging
        for (int i = 0; i < parts.length; i++) {
            System.out.println("Part " + (i + 1) + " (" + parts[i].split("\\s+").length + " words): " + 
                             parts[i].substring(0, Math.min(50, parts[i].length())) + "...");
        }
        
        return parts;
    }

    /**
     * Legacy method for backward compatibility - now redirects to 5 parts
     */
    @Deprecated
    public String[] splitTextIntoThreeParts(String text) {
        System.out.println("⚠️ Warning: splitTextIntoThreeParts is deprecated. Use splitTextIntoFiveParts instead.");
        return splitTextIntoFiveParts(text);
    }
}