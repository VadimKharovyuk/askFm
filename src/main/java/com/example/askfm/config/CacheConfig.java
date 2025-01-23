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
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .expireAfterAccess(10, TimeUnit.MINUTES)
                .initialCapacity(100)
                .maximumSize(1000)
                .recordStats(); // Для мониторинга
    }

    @Bean
    public CacheManager cacheManager(Caffeine<Object, Object> caffeine) {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(
                "users",           // Кеш для пользователей
                "suggestions",     // Кеш для рекомендаций
                "followers",       // Кеш для подписчиков
                "userProfiles",     // Кеш для профилей
                "userSearch" // добавляем кеш для поиска

        );
        cacheManager.setCaffeine(caffeine);
        return cacheManager;
    }


}