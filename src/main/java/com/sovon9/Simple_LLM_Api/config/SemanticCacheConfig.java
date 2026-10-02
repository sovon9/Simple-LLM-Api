package com.sovon9.Simple_LLM_Api.config;

import org.springframework.ai.chat.cache.semantic.SemanticCache;
import org.springframework.ai.chat.cache.semantic.SemanticCacheAdvisor;
import org.springframework.ai.vectorstore.redis.cache.semantic.DefaultSemanticCache;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SemanticCacheConfig {

//    @Bean
//    public SemanticCache semanticCache()
//    {
//        return DefaultSemanticCache.builder()
//                .jedisClient()
//    }
//
//    @Bean
//    public SemanticCacheAdvisor cacheAdvisor()
//    {
//        return SemanticCacheAdvisor.builder()
//    }

}
