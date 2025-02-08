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
    private final TransactionService transactionService;


    public PhotoDTO createPhoto(CreatePhotoRequest request, String username, byte[] imageData) throws IOException {
        User owner = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Photo photo = photoMapper.toEntity(request, owner, imageData);
        Photo savedPhoto = photoRepository.save(photo);

        return photoMapper.toDTO(savedPhoto, owner); // Передаем владельца
    }


    @Transactional(readOnly = true)
    public PhotoDTO getPhoto(Long photoId, String username) {
        Photo photo = photoRepository.findById(photoId)
                .orElseThrow(() -> new RuntimeException("Photo not found"));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return photoMapper.toDTO(photo, user); // Маппер сам решит, блюрить фото или нет
    }


    @Transactional
    public PhotoDTO unlockPhoto(Long photoId, String username) {
        Photo photo = photoRepository.findById(photoId)
                .orElseThrow(() -> new RuntimeException("Photo not found"));

        User buyer = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        User seller = photo.getOwner();

        if (seller.getUsername().equals(username)) {
            throw new RuntimeException("You cannot unlock your own photo");
        }

        if (unlockedPhotoRepository.existsByUserAndPhoto(buyer, photo)) {
            throw new RuntimeException("You have already unlocked this photo");
        }

        if (buyer.getBalance().compareTo(photo.getPrice()) < 0) {
            throw new RuntimeException("Insufficient balance to unlock photo");
        }

        // Объявляем переменную до try-catch блока
        TransactionDTO transactionDTO = null;

        try {
            // Создаем транзакцию через сервис
            transactionDTO = transactionService.createTransaction(
                    buyer,
                    seller,
                    photo,
                    photo.getPrice(),
                    TransactionType.PHOTO_PURCHASE
            );

            // Обновляем балансы
            buyer.setBalance(buyer.getBalance().subtract(photo.getPrice()));
            seller.setBalance(seller.getBalance().add(photo.getPrice()));
            userRepository.save(buyer);
            userRepository.save(seller);

            // Создаем запись о разблокировке
            UnlockedPhoto unlockedPhoto = new UnlockedPhoto();
            unlockedPhoto.setUser(buyer);
            unlockedPhoto.setPhoto(photo);
            unlockedPhoto.setUnlockedAt(LocalDateTime.now());
            unlockedPhotoRepository.save(unlockedPhoto);

            // Обновляем статус транзакции
            transactionService.updateTransactionStatus(transactionDTO.getId(), TransactionStatus.COMPLETED);

            return photoMapper.toDTO(photo, buyer);

        } catch (Exception e) {
            // Проверяем, была ли создана транзакция
            if (transactionDTO != null) {
                // В случае ошибки помечаем транзакцию как неудачную
                transactionService.updateTransactionStatus(transactionDTO.getId(), TransactionStatus.FAILED);
            }
            throw new RuntimeException("Failed to process transaction: " + e.getMessage());
        }
    }

//    @Transactional
//    public PhotoDTO unlockPhoto(Long photoId, String username) {
//        Photo photo = photoRepository.findById(photoId)
//                .orElseThrow(() -> new RuntimeException("Photo not found"));
//
//        User user = userRepository.findByUsername(username)
//                .orElseThrow(() -> new RuntimeException("User not found"));
//
//        // Проверяем, не является ли пользователь владельцем
//        if (photo.getOwner().getUsername().equals(username)) {
//            throw new RuntimeException("You cannot unlock your own photo");
//        }
//
//        // Проверяем, не разблокировано ли уже фото для этого пользователя
//        if (unlockedPhotoRepository.existsByUserAndPhoto(user, photo)) {
//            throw new RuntimeException("You have already unlocked this photo");
//        }
//
//        // Проверяем баланс
//        if (user.getBalance().compareTo(photo.getPrice()) < 0) {
//            throw new RuntimeException("Insufficient balance to unlock photo");
//        }
//
//        // Выполняем транзакцию
//        try {
//            // Списываем монеты
//            user.setBalance(user.getBalance().subtract(photo.getPrice()));
//            userRepository.save(user);
//
//            // Начисляем монеты владельцу
//            User owner = photo.getOwner();
//            owner.setBalance(owner.getBalance().add(photo.getPrice()));
//            userRepository.save(owner);
//
//            // Создаем запись о разблокировке
//            UnlockedPhoto unlockedPhoto = new UnlockedPhoto();
//            unlockedPhoto.setUser(user);
//            unlockedPhoto.setPhoto(photo);
//            unlockedPhoto.setUnlockedAt(LocalDateTime.now());
//            unlockedPhotoRepository.save(unlockedPhoto);
//
//            return photoMapper.toDTO(photo, user);
//        } catch (Exception e) {
//            throw new RuntimeException("Failed to process transaction: " + e.getMessage());
//        }
//    }

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



    @Transactional(readOnly = true)
    public Photo getPhotoEntity(Long photoId) {
        return photoRepository.findById(photoId)
                .orElseThrow(() -> new RuntimeException("Photo not found"));
    }
    }