package com.recommendation.homestay.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path uploadPath = UploadUtils.getUploadDir().toAbsolutePath().normalize();
        String uploadLocation = "file:" + uploadPath.toString() + "/";
        registry.addResourceHandler("/api/uploads/**", "/uploads/**")
                .addResourceLocations(uploadLocation);
    }
}
