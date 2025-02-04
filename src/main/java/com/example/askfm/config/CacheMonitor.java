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

//    public void showCacheStats(String cacheName) {
//        CaffeineCache cache = (CaffeineCache) cacheManager.getCache(cacheName);
//        if (cache != null) {
//            CacheStats stats = cache.getNativeCache().stats();
//            log.debug("Cache '{}' statistics:", cacheName);
//            log.debug("Hit count: {}", stats.hitCount());
//            log.debug("Miss count: {}", stats.missCount());
//            log.debug("Load success count: {}", stats.loadSuccessCount());
//            log.debug("Load failure count: {}", stats.loadFailureCount());
//            log.debug("Total load time: {}ms", stats.totalLoadTime() / 1_000_000);
//            log.debug("Eviction count: {}", stats.evictionCount());
//            log.debug("Eviction weight: {}", stats.evictionWeight());
//        }
//    }
//
//    @Scheduled(fixedRate = TWO_MINUTES_IN_MS)
//    public void logCacheStatistics() {
//        cacheManager.getCacheNames().forEach(this::showCacheStats);
//    }
}
