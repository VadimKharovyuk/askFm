package com.example.askfm.service;

import com.example.askfm.dto.PhotoStatDTO;
import com.example.askfm.dto.PhotoUnlockStatDTO;
import com.example.askfm.maper.PhotoMapper;
import com.example.askfm.model.Photo;
import com.example.askfm.model.User;
import com.example.askfm.repository.PhotoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    private final ImageService imageService;
    private final PhotoMapper photoMapper;


//по загрузкам
    public Page<PhotoStatDTO> getTopPhotoUploaders(Pageable pageable) {
        log.info("🔍 Получение статистики по загрузкам фото");
        Page<Object[]> topUploaders = photoRepository.findTopUsersByUploadedPhotos(pageable);
        return photoMapper.toPhotoStatDTOPage(topUploaders);
    }

//разблокированые
    public Page<PhotoUnlockStatDTO> getTopUnlockedPhotos(Pageable pageable) {
        log.info("🔍 Получение статистики по разблокировкам фото");
        Page<Object[]> topPhotos = photoRepository.findTopPhotosByUnlocks(pageable);
        return photoMapper.toUnlockStatDTOPage(topPhotos);
    }


}