package com.example.askfm.service;

import com.example.askfm.dto.PhotoStatDTO;
import com.example.askfm.dto.PhotoUnlockStatDTO;
import com.example.askfm.maper.PhotoMapper;
import com.example.askfm.model.Photo;
import com.example.askfm.model.User;
import com.example.askfm.repository.PhotoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class PhotoStatisticsService {
    private final PhotoRepository photoRepository;
    private final PhotoMapper photoMapper;
    private final CacheManager cacheManager;

    @SuppressWarnings("unchecked")
    public Page<PhotoStatDTO> getTopPhotoUploaders(Pageable pageable) {
        String cacheKey = "photoUploads_" + pageable.getPageNumber() + "_" + pageable.getPageSize();
        Cache cache = cacheManager.getCache("photoUploadsStats");

        if (cache != null) {
            Cache.ValueWrapper cachedValue = cache.get(cacheKey);
            if (cachedValue != null) {
                log.info("📦 Получены данные из кеша для статистики загрузок, ключ: {}", cacheKey);
                return (Page<PhotoStatDTO>) cachedValue.get();
            }
        }

        log.info("🔍 Получение статистики по загрузкам фото из БД");
        Page<Object[]> topUploaders = photoRepository.findTopUsersByUploadedPhotos(pageable);
        Page<PhotoStatDTO> result = photoMapper.toPhotoStatDTOPage(topUploaders);

        if (cache != null) {
            cache.put(cacheKey, result);
            log.info("💾 Сохранены данные в кеш для статистики загрузок, ключ: {}", cacheKey);
        }

        return result;
    }
    @SuppressWarnings("unchecked")
    public Page<PhotoUnlockStatDTO> getTopUnlockedPhotos(Pageable pageable) {
        String cacheKey = "photoUnlocks_" + pageable.getPageNumber() + "_" + pageable.getPageSize();
        Cache cache = cacheManager.getCache("photoUnlocksStats");

        if (cache != null) {
            Cache.ValueWrapper cachedValue = cache.get(cacheKey);
            if (cachedValue != null) {
                log.info("📦 Получены данные из кеша для статистики разблокировок, ключ: {}", cacheKey);
                return (Page<PhotoUnlockStatDTO>) cachedValue.get();
            }
        }

        log.info("🔍 Получение статистики по разблокировкам фото из БД");
        Page<Object[]> topPhotos = photoRepository.findTopPhotosByUnlocks(pageable);
        Page<PhotoUnlockStatDTO> result = photoMapper.toUnlockStatDTOPage(topPhotos);

        if (cache != null) {
            cache.put(cacheKey, result);
            log.info("💾 Сохранены данные в кеш для статистики разблокировок, ключ: {}", cacheKey);
        }

        return result;
    }


////по загрузкам
//    public Page<PhotoStatDTO> getTopPhotoUploaders(Pageable pageable) {
//        log.info("🔍 Получение статистики по загрузкам фото");
//        Page<Object[]> topUploaders = photoRepository.findTopUsersByUploadedPhotos(pageable);
//        return photoMapper.toPhotoStatDTOPage(topUploaders);
//    }
//
//
////разблокированые
//    public Page<PhotoUnlockStatDTO> getTopUnlockedPhotos(Pageable pageable) {
//        log.info("🔍 Получение статистики по разблокировкам фото");
//        Page<Object[]> topPhotos = photoRepository.findTopPhotosByUnlocks(pageable);
//        return photoMapper.toUnlockStatDTOPage(topPhotos);
//    }


}