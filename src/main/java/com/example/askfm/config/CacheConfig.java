package com.example.askfm.config;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.aspectj.weaver.tools.cache.CacheStatistics;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public Caffeine<Object, Object> caffeineConfig() {
        return Caffeine.newBuilder()
                .expireAfterWrite(10, TimeUnit.MINUTES)  // было 5 минут
                .expireAfterAccess(15, TimeUnit.MINUTES) // было 10 минут
                .initialCapacity(100)
                .maximumSize(1000)
                .recordStats();
    }

    @Bean
    public CacheManager cacheManager(Caffeine<Object, Object> caffeine) {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(
                "users",           // Кэш для сущностей пользователей
                "suggestions",     // Кэш для рекомендаций
                "followers",       // Кэш для подписчиков
                "userProfiles",    // Кэш для профилей
                "userSearch"       // Кэш для поиска
        );
        cacheManager.setCaffeine(caffeine);
        return cacheManager;
    }


}