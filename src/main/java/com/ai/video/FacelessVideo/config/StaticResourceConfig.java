package com.ai.video.FacelessVideo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(@SuppressWarnings("null") ResourceHandlerRegistry registry) {
        // Serve files from output directory
        registry.addResourceHandler("/output/**")
                .addResourceLocations("file:output/")
                .setCachePeriod(0); // No caching for development
        
        // Serve files from assets directory
        registry.addResourceHandler("/assets/**")
                .addResourceLocations("file:assets/")
                .setCachePeriod(0); // No caching for development
    }
}