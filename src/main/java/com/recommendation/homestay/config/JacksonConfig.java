package com.recommendation.homestay.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.hibernate5.Hibernate5Module;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

@Configuration
public class JacksonConfig {

    @Bean
    public ObjectMapper objectMapper(Jackson2ObjectMapperBuilder builder) {
        ObjectMapper objectMapper = builder.createXmlMapper(false).build();
        
        // Register Hibernate5Module to handle lazy-loaded entities properly
        Hibernate5Module hibernate5Module = new Hibernate5Module();
        // Disable forcing lazy loading to avoid triggering lazy initialization during serialization
        hibernate5Module.configure(Hibernate5Module.Feature.FORCE_LAZY_LOADING, false);
        // Serialize identifier for not-loaded lazy associations instead of null
        hibernate5Module.configure(Hibernate5Module.Feature.SERIALIZE_IDENTIFIER_FOR_LAZY_NOT_LOADED_OBJECTS, true);
        
        objectMapper.registerModule(hibernate5Module);
        
        return objectMapper;
    }
}
