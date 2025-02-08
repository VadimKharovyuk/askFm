package com.example.askfm.service;


import com.example.askfm.dto.CreatePhotoRequest;
import com.example.askfm.dto.PhotoDTO;
import com.example.askfm.maper.PhotoMapper;
import com.example.askfm.model.Photo;
import com.example.askfm.model.UnlockedPhoto;
import com.example.askfm.model.User;
import com.example.askfm.repository.PhotoRepository;
import com.example.askfm.repository.UnlockedPhotoRepository;
import com.example.askfm.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
@Transactional
public class PhotoService {
    private final PhotoRepository photoRepository;
    private final UserRepository userRepository;
    private final PhotoMapper photoMapper;
    private final UnlockedPhotoRepository unlockedPhotoRepository;


    public PhotoDTO createPhoto(CreatePhotoRequest request, String username, byte[] imageData) throws IOException {
        User owner = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Photo photo = photoMapper.toEntity(request, owner, imageData);
        Photo savedPhoto = photoRepository.save(photo);

        return photoMapper.toDTO(savedPhoto, owner); // Передаем владельца
    }

//    public PhotoDTO createPhoto(CreatePhotoRequest request, String username, byte[] imageData) throws IOException {
//        User owner = userRepository.findByUsername(username)
//                .orElseThrow(() -> new RuntimeException("User not found"));
//
//        Photo photo = photoMapper.toEntity(request, owner, imageData);
//        Photo savedPhoto = photoRepository.save(photo);
//
//        return photoMapper.toDTO(savedPhoto);
//    }

    @Transactional(readOnly = true)
    public PhotoDTO getPhoto(Long photoId, String username) {
        Photo photo = photoRepository.findById(photoId)
                .orElseThrow(() -> new RuntimeException("Photo not found"));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return photoMapper.toDTO(photo, user); // Маппер сам решит, блюрить фото или нет
    }
//    @Transactional(readOnly = true)
//    public PhotoDTO getPhoto(Long photoId, String username) {
//        Photo photo = photoRepository.findById(photoId)
//                .orElseThrow(() -> new RuntimeException("Photo not found"));
//
//        User user = userRepository.findByUsername(username)
//                .orElseThrow(() -> new RuntimeException("User not found"));
//
//        // Проверяем условия доступа:
//        // 1. Пользователь является владельцем фото
//        // 2. Фото не заблокировано
//        // 3. Пользователь разблокировал это фото
//        boolean hasAccess = photo.getOwner().getUsername().equals(username) ||
//                !photo.getIsLocked() ||
//                unlockedPhotoRepository.existsByUserAndPhoto(user, photo);
//
//        if (!hasAccess) {
//            // Если нет доступа - возвращаем DTO без изображения
//            return photoMapper.toDTOWithoutImage(photo);
//        }
//
//        // Если есть доступ - возвращаем полное DTO с изображением
//        return photoMapper.toDTO(photo);
//    }



    @Transactional
    public PhotoDTO unlockPhoto(Long photoId, String username) {
        Photo photo = photoRepository.findById(photoId)
                .orElseThrow(() -> new RuntimeException("Photo not found"));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Проверяем, не является ли пользователь владельцем
        if (photo.getOwner().getUsername().equals(username)) {
            throw new RuntimeException("You cannot unlock your own photo");
        }

        // Проверяем, не разблокировано ли уже фото для этого пользователя
        if (unlockedPhotoRepository.existsByUserAndPhoto(user, photo)) {
            throw new RuntimeException("You have already unlocked this photo");
        }

        // Проверяем баланс
        if (user.getBalance().compareTo(photo.getPrice()) < 0) {
            throw new RuntimeException("Insufficient balance to unlock photo");
        }

        // Выполняем транзакцию
        try {
            // Списываем монеты
            user.setBalance(user.getBalance().subtract(photo.getPrice()));
            userRepository.save(user);

            // Начисляем монеты владельцу
            User owner = photo.getOwner();
            owner.setBalance(owner.getBalance().add(photo.getPrice()));
            userRepository.save(owner);

            // Создаем запись о разблокировке
            UnlockedPhoto unlockedPhoto = new UnlockedPhoto();
            unlockedPhoto.setUser(user);
            unlockedPhoto.setPhoto(photo);
            unlockedPhoto.setUnlockedAt(LocalDateTime.now());
            unlockedPhotoRepository.save(unlockedPhoto);

            return photoMapper.toDTO(photo, user);
        } catch (Exception e) {
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

    public Page<PhotoDTO> getUserPhotosByUsername(String username, String currentUsername, Pageable pageable) {
        Page<Photo> photoPage = photoRepository.findByOwnerUsername(username, pageable);

        User currentUser = null;
        if (currentUsername != null) {
            currentUser = userRepository.findByUsername(currentUsername)
                    .orElseThrow(() -> new RuntimeException("Current user not found"));
        }

        User finalCurrentUser = currentUser;
        return photoPage.map(photo -> photoMapper.toDTO(photo, finalCurrentUser));
    }

//    public Page<PhotoDTO> getUserPhotosByUsername(String username, Pageable pageable) {
//        // Получаем страницу фотографий пользователя по username
//        Page<Photo> photoPage = photoRepository.findByOwnerUsername(username, pageable);
//
//        // Преобразуем каждое фото в DTO
//        return photoPage.map(photoMapper::toDTO);
//    }

    
    

    private void validatePhotoUnlock(Photo photo, User user) {
        if (!photo.getIsLocked()) {
            throw new RuntimeException("Photo is already unlocked");
        }
        if (user.getBalance().compareTo(photo.getPrice()) < 0) {
            throw new RuntimeException("Insufficient balance");
        }
    }

    private void processPayment(User buyer, User seller, BigDecimal amount) {
        buyer.setBalance(buyer.getBalance().subtract(amount));
        seller.setBalance(seller.getBalance().add(amount));
        userRepository.save(buyer);
        userRepository.save(seller);
    }

    @Transactional(readOnly = true)
    public Photo getPhotoEntity(Long photoId) {
        return photoRepository.findById(photoId)
                .orElseThrow(() -> new RuntimeException("Photo not found"));
    }
    }