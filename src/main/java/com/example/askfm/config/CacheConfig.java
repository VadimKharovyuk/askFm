package com.example.askfm.config;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.weaver.tools.cache.CacheStatistics;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
@Slf4j
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();

        // Конфигурация для общего кэша
        Caffeine<Object, Object> defaultCacheBuilder = Caffeine.newBuilder()
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .expireAfterAccess(15, TimeUnit.MINUTES)
                .initialCapacity(100)
                .maximumSize(1000)
                .recordStats();

        // Конфигурация для кэша followers
        Caffeine<Object, Object> followersCacheBuilder = Caffeine.newBuilder()
                .expireAfterWrite(30, TimeUnit.MINUTES)
                .expireAfterAccess(45, TimeUnit.MINUTES)
                .initialCapacity(200)
                .maximumSize(2000)
                .recordStats();

        // Создаем кэши с разными конфигурациями
        cacheManager.setCacheNames(Arrays.asList( "userSearch", "followers"));
        cacheManager.setCaffeine(defaultCacheBuilder);

        // Устанавливаем специальную конфигурацию для followers
        cacheManager.registerCustomCache("followers", followersCacheBuilder.build());

        return cacheManager;
    }
}