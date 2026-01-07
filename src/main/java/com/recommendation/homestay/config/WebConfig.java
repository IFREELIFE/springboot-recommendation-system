package com.recommendation.homestay.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String uploadPath = UploadUtils.getUploadDir().toUri().toString();
        if (!uploadPath.endsWith("/")) {
            uploadPath = uploadPath + "/";
        }
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadPath);
    }
}
