package com.recommendation.homestay.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus Configuration
 * 
 * Configures MyBatis-Plus for database operations in the application.
 * This is the primary ORM configuration - all database operations use MyBatis-Plus.
 * 
 * Features configured:
 * - Automatic pagination support for MySQL
 * - Mapper scanning for dependency injection
 * - Database type specific optimizations
 * 
 * @author Homestay Recommendation System
 */
@Configuration
@MapperScan("com.recommendation.homestay.mapper")
public class MyBatisPlusConfig {

    /**
     * MyBatis-Plus pagination plugin
     * 
     * Provides automatic pagination support for query methods.
     * Optimized for MySQL database with MySQL-specific pagination strategies.
     * 
     * @return Configured MybatisPlusInterceptor with pagination support
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }
}
