package com.example.askfm.maper;
import com.example.askfm.dto.CreatePhotoRequest;
import com.example.askfm.dto.PhotoDTO;
import com.example.askfm.model.Photo;
import com.example.askfm.model.User;
import com.example.askfm.service.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Base64;
@Component
@RequiredArgsConstructor
public class PhotoMapper {
    private final ImageService imageService;

    public PhotoDTO toDTO(Photo photo) {
        return PhotoDTO.builder()
                .id(photo.getId())
                .ownerId(photo.getOwner().getId())
                .ownerUsername(photo.getOwner().getUsername())
                .photoBase64(imageService.getBase64Avatar(photo.getPhoto()))
                .price(photo.getPrice())
                .isLocked(photo.getIsLocked())
                .description(photo.getDescription())
                .createdAt(photo.getCreatedAt())
                .isNSFW(photo.getIsNSFW())
                .build();
    }

    public Photo toEntity(CreatePhotoRequest request, User owner, byte[] photoData) throws IOException {
        // Обработка изображения при создании сущности
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
    // Добавьте метод для DTO без изображения
    public PhotoDTO toDTOWithoutImage(Photo photo) {
        return PhotoDTO.builder()
                .id(photo.getId())
                .ownerId(photo.getOwner().getId())
                .ownerUsername(photo.getOwner().getUsername())
                .photoBase64(null) // Не включаем изображение
                .price(photo.getPrice())
                .isLocked(true)
                .description(photo.getDescription())
                .createdAt(photo.getCreatedAt())
                .isNSFW(photo.getIsNSFW())
                .build();
    }
}