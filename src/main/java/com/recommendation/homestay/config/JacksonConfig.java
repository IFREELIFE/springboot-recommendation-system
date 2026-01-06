package com.recommendation.homestay.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.hibernate5.Hibernate5Module;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

@Configuration
public class JacksonConfig {

    @Bean
    public MappingJackson2HttpMessageConverter jackson2HttpMessageConverter() {
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        ObjectMapper objectMapper = new ObjectMapper();
        
        // Register Hibernate5Module to handle Hibernate proxy objects
        Hibernate5Module hibernateModule = new Hibernate5Module();
        // Enable: serialize identifier for lazy not loaded objects (avoids null values)
        hibernateModule.enable(Hibernate5Module.Feature.SERIALIZE_IDENTIFIER_FOR_LAZY_NOT_LOADED_OBJECTS);
        objectMapper.registerModule(hibernateModule);
        
        // Disable FAIL_ON_EMPTY_BEANS to avoid errors when serializing empty objects
        objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        
        converter.setObjectMapper(objectMapper);
        return converter;
    }
}
