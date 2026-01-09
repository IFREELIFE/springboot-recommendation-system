package com.recommendation.homestay.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus 配置
 *
 * 为应用配置 MyBatis-Plus 以处理数据库操作，这是主要的 ORM 配置。
 *
 * 配置要点：
 * - MySQL 分页插件
 * - Mapper 扫描以便依赖注入
 * - 数据库类型的专项优化
 *
 * @author Homestay Recommendation System
 */
@Configuration
@MapperScan("com.recommendation.homestay.mapper")
public class MyBatisPlusConfig {

    /**
     * MyBatis-Plus 分页插件
     *
     * 为查询方法提供自动分页能力，基于 MySQL 分页策略做了优化。
     *
     * @return 配置好分页能力的 MybatisPlusInterceptor
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }
}
