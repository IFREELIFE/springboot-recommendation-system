package com.recommendation.homestay;

import com.recommendation.homestay.config.JacksonConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@EnableCaching
// 显式导入Jackson配置类，优先级最高
@Import(JacksonConfig.class)
public class HomestayRecommendationApplication {

    public static void main(String[] args) {
        SpringApplication.run(HomestayRecommendationApplication.class, args);
    }
}