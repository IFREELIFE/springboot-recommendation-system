package com.recommendation.homestay.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI baseOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("民宿推荐系统 API")
                        .description("接口文档：包含民宿推荐系统的业务接口说明")
                        .version("v1"));
    }

    @Bean
    public GroupedOpenApi recommendationApi() {
        return GroupedOpenApi.builder()
                .group("recommendation")
                .pathsToMatch("/api/recommendations/**")
                .build();
    }

    @Bean
    public GroupedOpenApi otherApis() {
        return GroupedOpenApi.builder()
                .group("other-apis")
                .pathsToMatch("/api/**")
                .pathsToExclude("/api/recommendations/**")
                .build();
    }
}
