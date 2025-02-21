package com.example.askfm.service;

import com.example.askfm.dto.TestPhotoDTO;
import com.example.askfm.model.TestPhoto;
import com.example.askfm.repository.TestPhotoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class TestPhotoService {
    private final ImgurStorageService imgurStorageService;
    private final TestPhotoRepository testPhotoRepository;

    public TestPhotoDTO savePhoto(String title, String description, MultipartFile file) {
        try {
            // Загружаем фото в Imgur и получаем результат
            ImgurStorageService.ImgurUploadResult uploadResult =
                    imgurStorageService.saveImage(file.getBytes());

            // Создаем запись в базе данных
            TestPhoto photo = TestPhoto.builder()
                    .title(title)
                    .description(description)
                    .imageUrl(uploadResult.getImageUrl())
                    .deleteHash(uploadResult.getDeleteHash()) // Сохраняем deleteHash
                    .createdAt(LocalDateTime.now())
                    .build();

            TestPhoto savedPhoto = testPhotoRepository.save(photo);

            log.info("Saved test photo with ID: {} and Imgur URL: {}",
                    savedPhoto.getId(), uploadResult.getImageUrl());

            return convertToDTO(savedPhoto);
        } catch (IOException e) {
            log.error("Error saving test photo: ", e);
            throw new RuntimeException("Failed to save photo: " + e.getMessage());
        }
    }
    // Добавим метод удаления фото
    public void deletePhoto(Long id) {
        TestPhoto photo = testPhotoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Photo not found"));

        // Удаляем с Imgur используя deleteHash
        imgurStorageService.deleteImage(photo.getDeleteHash());

        // Удаляем из базы данных
        testPhotoRepository.delete(photo);
    }

    public TestPhotoDTO getPhoto(Long id) {
        TestPhoto photo = testPhotoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Photo not found"));
        return convertToDTO(photo);
    }

    private TestPhotoDTO convertToDTO(TestPhoto photo) {
        return TestPhotoDTO.builder()
                .id(photo.getId())
                .title(photo.getTitle())
                .description(photo.getDescription())
                .imageUrl(photo.getImageUrl())
                .createdAt(photo.getCreatedAt())
                .build();
    }
}
