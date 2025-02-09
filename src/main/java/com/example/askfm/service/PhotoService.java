package com.example.askfm.service;


import com.example.askfm.dto.CreatePhotoRequest;
import com.example.askfm.dto.PhotoDTO;
import com.example.askfm.dto.TransactionDTO;
import com.example.askfm.enums.TransactionStatus;
import com.example.askfm.enums.TransactionType;
import com.example.askfm.maper.PhotoMapper;
import com.example.askfm.model.Photo;
import com.example.askfm.model.UnlockedPhoto;
import com.example.askfm.model.User;
import com.example.askfm.repository.PhotoRepository;
import com.example.askfm.repository.TransactionRepository;
import com.example.askfm.repository.UnlockedPhotoRepository;
import com.example.askfm.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.function.Function;
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PhotoService {
    private final PhotoRepository photoRepository;
    private final UserRepository userRepository;
    private final PhotoMapper photoMapper;
    private final UnlockedPhotoRepository unlockedPhotoRepository;
    private final TransactionService transactionService;
    private final NotificationService notificationService;
    private final CacheManager cacheManager;


    // Метод создания фото
    public PhotoDTO createPhoto(CreatePhotoRequest request, String username, byte[] imageData) throws IOException {
        log.debug("📸 Начало создания фото для пользователя: {}", username);

        User owner = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Photo photo = photoMapper.toEntity(request, owner, imageData);
        Photo savedPhoto = photoRepository.save(photo);
        log.debug("✅ Фото успешно сохранено в БД, id: {}", savedPhoto.getId());

        // Очищаем кеш фотографий пользователя
        Cache photoCache = cacheManager.getCache("userPhotos");
        if (photoCache != null) {
            photoCache.invalidate(); // Полная очистка кеша
            log.debug("🗑️ Кеш фотографий полностью очищен после создания нового фото");
        } else {
            log.warn("⚠️ Кеш фотографий недоступен");
        }

        PhotoDTO photoDTO = photoMapper.toDTO(savedPhoto, owner);
        log.debug("🎉 Фото успешно создано и обработано, описание: {}", photoDTO.getDescription());

        return photoDTO;
    }


    @Transactional(readOnly = true)
    public PhotoDTO getPhoto(Long photoId, String username) {
        Photo photo = photoRepository.findById(photoId)
                .orElseThrow(() -> new RuntimeException("Photo not found"));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return photoMapper.toDTO(photo, user);
    }

    // Метод разблокировки фото
    @Transactional
    public PhotoDTO unlockPhoto(Long photoId, String username) {
        log.debug("🔓 Начало процесса разблокировки фото id: {} для пользователя: {}", photoId, username);

        Photo photo = photoRepository.findById(photoId)
                .orElseThrow(() -> new RuntimeException("Photo not found"));
        log.debug("📸 Фото найдено, владелец: {}", photo.getOwner().getUsername());

        User buyer = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        User seller = photo.getOwner();

        if (seller.getUsername().equals(username)) {
            log.warn("⚠️ Попытка разблокировать собственное фото пользователем: {}", username);
            throw new RuntimeException("You cannot unlock your own photo");
        }

        if (unlockedPhotoRepository.existsByUserAndPhoto(buyer, photo)) {
            log.warn("⚠️ Попытка повторной разблокировки фото id: {} пользователем: {}", photoId, username);
            throw new RuntimeException("You have already unlocked this photo");
        }

        if (buyer.getBalance().compareTo(photo.getPrice()) < 0) {
            log.warn("⚠️ Недостаточный баланс у пользователя: {}", username);
            throw new RuntimeException("Insufficient balance to unlock photo");
        }

        TransactionDTO transactionDTO = null;

        try {
            log.debug("💰 Начало обработки транзакции для фото id: {}", photoId);

            transactionDTO = transactionService.createTransaction(
                    buyer,
                    seller,
                    photo,
                    photo.getPrice(),
                    TransactionType.PHOTO_PURCHASE
            );

            buyer.setBalance(buyer.getBalance().subtract(photo.getPrice()));
            seller.setBalance(seller.getBalance().add(photo.getPrice()));
            userRepository.save(buyer);
            userRepository.save(seller);
            log.debug("✅ Балансы пользователей обновлены успешно");

            UnlockedPhoto unlockedPhoto = new UnlockedPhoto();
            unlockedPhoto.setUser(buyer);
            unlockedPhoto.setPhoto(photo);
            unlockedPhoto.setUnlockedAt(LocalDateTime.now());
            unlockedPhotoRepository.save(unlockedPhoto);
            log.debug("✅ Запись о разблокировке сохранена");

            transactionService.updateTransactionStatus(transactionDTO.getId(), TransactionStatus.COMPLETED);

            notificationService.notifyAboutPhotoEvent(buyer, photo);
            log.debug("📨 Уведомление о покупке фото отправлено");

            // Очищаем весь кеш фотографий
            Cache photoCache = cacheManager.getCache("userPhotos");
            if (photoCache != null) {
                photoCache.invalidate();
                log.debug("🗑️ Кеш фотографий полностью очищен после разблокировки");
            } else {
                log.warn("⚠️ Кеш фотографий недоступен");
            }

            PhotoDTO result = photoMapper.toDTO(photo, buyer);
            log.debug("🎉 Фото успешно разблокировано");
            return result;

        } catch (Exception e) {
            log.error("❌ Ошибка при разблокировке фото: {}", e.getMessage());
            if (transactionDTO != null) {
                transactionService.updateTransactionStatus(transactionDTO.getId(), TransactionStatus.FAILED);
                log.debug("❌ Статус транзакции обновлен на FAILED");
            }
            throw new RuntimeException("Failed to process transaction: " + e.getMessage());
        }
    }


    public Function<PhotoDTO, Boolean> createPhotoUnlockChecker(String username) {
        return photoDTO -> {
            if (username == null) return false;

            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            return unlockedPhotoRepository.existsByUserIdAndPhotoId(
                    user.getId(),
                    photoDTO.getId()
            );
        };
    }


    // Метод получения фото пользователя
    public Page<PhotoDTO> getUserPhotosByUsername(String username, String currentUsername, Pageable pageable) {
        Cache photoCache = cacheManager.getCache("userPhotos");
        String cacheKey = createCacheKey(username, currentUsername, pageable);

        // Пробуем получить из кеша
        Page<PhotoDTO> cachedPhotos = photoCache.get(cacheKey, Page.class);
        if (cachedPhotos != null) {
            log.debug("✅ Получены фото из кеша для пользователя {}, просматривающий: {}, страница {}",
                    username, currentUsername, pageable.getPageNumber());
            return cachedPhotos;
        }

        log.debug("⛔ Загрузка фото из БД для пользователя {}, просматривающий: {}, страница {}",
                username, currentUsername, pageable.getPageNumber());

        Page<Photo> photoPage = photoRepository.findByOwnerUsername(username, pageable);

        User currentUser = null;
        if (currentUsername != null) {
            currentUser = userRepository.findByUsername(currentUsername)
                    .orElseThrow(() -> new RuntimeException("Current user not found"));
        }

        User finalCurrentUser = currentUser;
        Page<PhotoDTO> photoDTOs = photoPage.map(photo -> photoMapper.toDTO(photo, finalCurrentUser));

        // Сохраняем в кеш
        photoCache.put(cacheKey, photoDTOs);
        log.debug("🔹 Сохранено в кеш {} фото для пользователя {}, просматривающий: {}, страница {}",
                photoDTOs.getContent().size(), username, currentUsername, pageable.getPageNumber());

        return photoDTOs;
    }
    private String createCacheKey(String username, String currentUsername, Pageable pageable) {
        return String.format("user_photos:%s:viewer:%s:page:%d:size:%d",
                username,
                currentUsername != null ? currentUsername : "anonymous",
                pageable.getPageNumber(),
                pageable.getPageSize());
    }


    @Transactional(readOnly = true)
    public Photo getPhotoEntity(Long photoId) {
        return photoRepository.findById(photoId)
                .orElseThrow(() -> new RuntimeException("Photo not found"));
    }
    }