package com.kma.itmanagement.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Serve uploaded profile images from uploads directory
        String uploadDir = "file:" + System.getProperty("user.dir") + "/uploads/avatars/";
        registry.addResourceHandler("/uploads/avatars/**")
                .addResourceLocations(uploadDir);
    }
}