package com.ai.video.FacelessVideo.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Service
public class ChatGPTService {

    @Value("${openai.api.key}")
    private String apiKey;
    
    /**
     * Generate Vietnamese article from headline (original method) - kept for backward compatibility
     */
    public String generateArticleFromHeadline(String headline) {
        String prompt = "Viết bài báo khoảng 200 từ dựa trên tiêu đề: \"" + headline + "\"";

        WebClient client = WebClient.create("https://api.openai.com");

        Map<String, Object> requestBody = Map.of(
            "model", "gpt-3.5-turbo",
            "messages", new Object[] {
                Map.of("role", "user", "content", prompt)
            }
        );

        return client.post()
                .uri("/v1/chat/completions")
                .header("Authorization", "Bearer " + apiKey)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> {
                    var choices = (java.util.List<Map<String, Object>>) response.get("choices");
                    return (String) ((Map<String, Object>) choices.get(0).get("message")).get("content");
                })
                .block();
    }

    /**
     * Generate Vietnamese article from headline - optimized for 30-second video with 5 parts (150-200 words)
     * This will create approximately 30 seconds of speech when divided into 5 parts (~6 seconds each)
     */
    public String generateVietnameseArticleFromHeadline(String headline) {
        try {
            String prompt = "Viết bài báo tiếng Việt chi tiết từ 150-200 từ dựa trên tiêu đề: \"" + headline + "\". " +
                          "Bài viết cần có cấu trúc rõ ràng với 5 phần: mở đầu, 3 phần nội dung chính, và kết luận. " +
                          "Không bao gồm các ký tự mở đầu,nội dung 1, nội dung 2, nội dung 3, kết luận ở đầu mỗi phần"+
                          "Mỗi phần nên có độ dài tương đương nhau để phù hợp chia thành 5 đoạn video ngắn. " +
                          "Sử dụng ngôn ngữ tự nhiên, dễ hiểu và phù hợp để đọc thành giọng nói nam. " +
                          "Tránh sử dụng ký hiệu đặc biệt hay số liệu phức tạp. " +
                          "Đảm bảo nội dung đầy đủ, chi tiết và hấp dẫn."+
                          "hãy giữ mỗi bài viết chỉ kéo dài khoảng 1-2 dòng, chỉ gói gọn trong tối đa 858 ký tự.";

            WebClient client = WebClient.create("https://api.openai.com");

            Map<String, Object> requestBody = Map.of(
                "model", "gpt-3.5-turbo",
                "messages", new Object[] {
                    Map.of("role", "system", "content", "Bạn là một nhà báo chuyên nghiệp viết bài tin tức tiếng Việt. " +
                           "Hãy viết bài chi tiết, đầy đủ thông tin và có cấu trúc rõ ràng phù hợp cho video 50 giây."),
                    Map.of("role", "user", "content", prompt)
                },
                "temperature", 0.7,
                "max_tokens", 500 // Increased for longer articles
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
                throw new RuntimeException("No choices in article generation response");
            }

            String article = (String) ((Map<String, Object>) choices.get(0).get("message")).get("content");
            
            System.out.println("✅ Vietnamese article generated successfully (optimized for 5 parts)");
            System.out.println("📝 Article length: " + article.length() + " characters");
            System.out.println("📖 Word count: " + article.split("\\s+").length + " words");
            System.out.println("⏱️ Expected video duration: ~30 seconds (5 parts × 6 seconds each)");
            System.out.println("📄 Preview: " + article.substring(0, Math.min(100, article.length())) + "...");
            
            return article.trim();

        } catch (Exception e) {
            System.err.println("❌ Vietnamese article generation failed: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Vietnamese article generation failed: " + e.getMessage());
        }
    }
}