package com.github.solenra.server.config;

import java.util.Arrays;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();
        // TODO configure caches as needed
        cacheManager.setCaches(Arrays.asList(
          new ConcurrentMapCache("exampleCacheOne"), 
          new ConcurrentMapCache("exampleCacheTwo")));
        return cacheManager;
    }

}
