package com.ai.video.FacelessVideo.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Service
public class TranslationService {

    @Value("${openai.api.key}")
    private String apiKey;

    public String translateToEnglish(String vietnameseText) {
        try {
            String prompt = "Translate the following Vietnamese text to English. Keep the same meaning and tone. Only return the English translation, nothing else:\n\n" + vietnameseText;

            WebClient client = WebClient.create("https://api.openai.com");

            Map<String, Object> requestBody = Map.of(
                "model", "gpt-3.5-turbo",
                "messages", new Object[] {
                    Map.of("role", "user", "content", prompt)
                },
                "temperature", 0.3
            );

            Map<String, Object> response = client.post()
                    .uri("/v1/chat/completions")
                    .header("Authorization", "Bearer " + apiKey)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response == null) {
                throw new RuntimeException("No response from OpenAI API");
            }

            var choices = (java.util.List<Map<String, Object>>) response.get("choices");
            if (choices == null || choices.isEmpty()) {
                throw new RuntimeException("No choices in translation response");
            }

            String translatedText = (String) ((Map<String, Object>) choices.get(0).get("message")).get("content");
            
            System.out.println("✅ Translation completed successfully");
            System.out.println("Original (first 100 chars): " + vietnameseText.substring(0, Math.min(100, vietnameseText.length())));
            System.out.println("Translated (first 100 chars): " + translatedText.substring(0, Math.min(100, translatedText.length())));
            
            return translatedText.trim();

        } catch (Exception e) {
            System.err.println("❌ Translation failed: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Translation failed: " + e.getMessage());
        }
    }
}