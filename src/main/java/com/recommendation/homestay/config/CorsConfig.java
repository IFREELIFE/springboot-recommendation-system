package com.recommendation.homestay.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration corsConfig = new CorsConfiguration();
        corsConfig.addAllowedOrigin("http://localhost:3000"); // 允许前端的域名（修改为你的客户端地址）
        corsConfig.addAllowedHeader("*"); // 允许所有头信息
        corsConfig.addAllowedMethod("*"); // 允许所有 HTTP 方法 (GET, POST, etc.)
        corsConfig.setAllowCredentials(true); // 允许携带凭据（如 Cookies）
        // 注册 CORS 配置
        source.registerCorsConfiguration("/**", corsConfig);
        return new CorsFilter(source);
    }
}
