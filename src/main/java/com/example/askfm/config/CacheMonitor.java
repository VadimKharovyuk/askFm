package com.example.askfm.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.stats.CacheStats;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
@Service
@Slf4j
@RequiredArgsConstructor
public class CacheMonitor {
    private final CacheManager cacheManager;
    private static final long TWO_MINUTES_IN_MS = 2 * 60 * 1000;

    @Scheduled(fixedRate = TWO_MINUTES_IN_MS)
    public void logCacheStatistics() {
        log.info("=== Звіт статистики кешу ===");
        cacheManager.getCacheNames().forEach(cacheName -> {
            CaffeineCache cache = (CaffeineCache) cacheManager.getCache(cacheName);
            if (cache != null) {
                CacheStats stats = cache.getNativeCache().stats();
                log.info("Статистика кешу '{}':", cacheName);
                log.info("  Відсоток попадань: {}%", String.format("%.2f", stats.hitRate() * 100));
                log.info("  Розмір: {}", cache.getNativeCache().estimatedSize());
                log.info("  Попадання: {}", stats.hitCount());
                log.info("  Промахи: {}", stats.missCount());
                log.info("  Вилучення: {}", stats.evictionCount());
                log.info("  Успішні завантаження: {}", stats.loadSuccessCount());
                log.info("  Помилки завантаження: {}", stats.loadFailureCount());
                log.info("  Загальний час завантаження: {} мс", stats.totalLoadTime() / 1_000_000);
                log.info("-----------------------------");
            }
        });
    }
}