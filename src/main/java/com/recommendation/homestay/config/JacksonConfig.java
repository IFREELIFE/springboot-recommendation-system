package com.recommendation.homestay.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Configuration
public class JacksonConfig {
    private static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    // 方案1：自定义 Jackson 构建器（优先级最高）
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jackson2ObjectMapperBuilderCustomizer() {
        return builder -> {
            // 注册 Java8 时间模块
            JavaTimeModule module = new JavaTimeModule();
            // 配置 LocalDateTime 序列化/反序列化
            LocalDateTimeSerializer localDateTimeSerializer = new LocalDateTimeSerializer(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT));
            LocalDateTimeDeserializer localDateTimeDeserializer = new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT));
            module.addSerializer(LocalDateTime.class, localDateTimeSerializer);
            module.addDeserializer(LocalDateTime.class, localDateTimeDeserializer);

            builder.modules(module);
            // 全局配置空值处理、时区等
            builder.failOnEmptyBeans(false);
            builder.failOnUnknownProperties(false);
            builder.timeZone("GMT+8");
        };
    }

    // 方案2：直接替换全局 ObjectMapper（兜底）
    @Bean
    public ObjectMapper objectMapper(Jackson2ObjectMapperBuilder builder) {
        ObjectMapper objectMapper = builder.build();
        // 双重确认注册模块
        JavaTimeModule module = new JavaTimeModule();
        LocalDateTimeSerializer serializer = new LocalDateTimeSerializer(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT));
        LocalDateTimeDeserializer deserializer = new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT));
        module.addSerializer(LocalDateTime.class, serializer);
        module.addDeserializer(LocalDateTime.class, deserializer);
        objectMapper.registerModule(module);
        return objectMapper;
    }
}
