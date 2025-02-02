package com.example.askfm.config;

import com.github.benmanes.caffeine.cache.Cache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class CacheMonitor {
    private final CacheManager cacheManager;

    public void showCacheStats(String cacheName) {
        CaffeineCache caffeineCache = (CaffeineCache) cacheManager.getCache(cacheName);
        if (caffeineCache != null) {
            Cache<Object, Object> nativeCache = caffeineCache.getNativeCache();
//            log.info("Cache: {} - Size: {}", cacheName, nativeCache.estimatedSize());
        }
    }
}
