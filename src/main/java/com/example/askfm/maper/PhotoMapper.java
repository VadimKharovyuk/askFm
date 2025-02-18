package com.example.askfm.maper;
import com.example.askfm.dto.CreatePhotoRequest;
import com.example.askfm.dto.PhotoDTO;
import com.example.askfm.dto.PhotoStatDTO;
import com.example.askfm.dto.PhotoUnlockStatDTO;
import com.example.askfm.model.Photo;
import com.example.askfm.model.User;
import com.example.askfm.repository.UnlockedPhotoRepository;
import com.example.askfm.service.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Base64;

@Component
@RequiredArgsConstructor
public class PhotoMapper {

    private final ImageService imageService;
    private final UnlockedPhotoRepository unlockedPhotoRepository;


    public PhotoDTO toDTO(Photo photo, User currentUser) {
        byte[] photoData;
        boolean isUnlocked = false;

        if (currentUser != null) {
            isUnlocked = photo.getOwner().equals(currentUser) ||
                    unlockedPhotoRepository.existsByUserAndPhoto(currentUser, photo);
        }

        try {
            if (photo.getIsLocked() && !isUnlocked) {
                // Для заблокированных и неразблокированных фото применяем размытие
                photoData = imageService.blurImage(photo.getPhoto());
            } else {
                // Для разблокированных или собственных фото показываем оригинал
                photoData = photo.getPhoto();
            }
        } catch (IOException e) {
            // В случае ошибки обработки изображения возвращаем null
            photoData = null;
        }

        return PhotoDTO.builder()
                .id(photo.getId())
                .ownerId(photo.getOwner().getId())
                .ownerUsername(photo.getOwner().getUsername())
                .photoBase64(imageService.getBase64Avatar(photoData))
                .price(photo.getPrice())
                .isLocked(photo.getIsLocked())
                .description(photo.getDescription())
                .createdAt(photo.getCreatedAt())
                .isNSFW(photo.getIsNSFW())
                .build();
    }


    public Photo toEntity(CreatePhotoRequest request, User owner, byte[] photoData) throws IOException {
        byte[] resizedImage = imageService.resizeImage(photoData, 1024);

        return Photo.builder()
                .owner(owner)
                .photo(resizedImage)
                .price(request.getPrice())
                .isLocked(true)
                .description(request.getDescription())
                .createdAt(LocalDateTime.now())
                .isNSFW(request.getIsNSFW())
                .build();
    }

    public PhotoUnlockStatDTO toUnlockStatDTO(Photo photo, Long unlockCount) {
        return PhotoUnlockStatDTO.builder()
                .photoId(photo.getId())
                .ownerUsername(photo.getOwner().getUsername())
                .description(photo.getDescription())
                .price(photo.getPrice())
                .unlockCount(unlockCount)
                .createdAt(photo.getCreatedAt())
                .build();
    }

    public Page<PhotoUnlockStatDTO> toUnlockStatDTOPage(Page<Object[]> pageResult) {
        return pageResult.map(result -> {
            Photo photo = (Photo) result[0];
            Long unlockCount = (Long) result[1];
            return toUnlockStatDTO(photo, unlockCount);
        });
    }

    public PhotoStatDTO toPhotoStatDTO(User user, Long totalPhotos) {
        String avatarBase64 = null;
        if (user.getAvatar() != null) {
            avatarBase64 = "data:image/jpeg;base64," + imageService.getBase64Avatar(user.getAvatar());
        }


        return PhotoStatDTO.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .userAvatar(avatarBase64)
                .totalPhotos(totalPhotos)
                .build();
    }

    // Маппинг для страницы статистики
    public Page<PhotoStatDTO> toPhotoStatDTOPage(Page<Object[]> pageResult) {
        return pageResult.map(result -> {
            User user = (User) result[0];
            Long count = (Long) result[1];
            return toPhotoStatDTO(user, count);
        });
    }
}
