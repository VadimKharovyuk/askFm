package com.example.askfm.service;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
@Slf4j
@Service
@RequiredArgsConstructor
public class ImgurStorageService {

    @Value("${imgur.client-id}")
    private String clientId;

    @Value("${imgur.access-token}")
    private String accessToken;

    private final RestTemplate restTemplate;



    public ImgurUploadResult saveImage(byte[] imageData) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.add("Authorization", "Bearer " + accessToken);
            headers.add("Client-ID", clientId);
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("image", new ByteArrayResource(imageData) {
                @Override
                public String getFilename() {
                    return "image.jpg";
                }
            });

            HttpEntity<MultiValueMap<String, Object>> requestEntity =
                    new HttpEntity<>(body, headers);

            ResponseEntity<ImgurResponse> response = restTemplate.exchange(
                    "https://api.imgur.com/3/image",
                    HttpMethod.POST,
                    requestEntity,
                    ImgurResponse.class
            );

            // Добавляем отладочную информацию
            log.info("Imgur API Response: {}", response.getBody());

            if (response.getBody() != null && response.getBody().getData() != null) {
                ImgurData data = response.getBody().getData();

                log.info("Imgur Response Data - Link: {}, DeleteHash: {}, ID: {}",
                        data.getLink(),
                        data.getDeletehash(),
                        data.getId());

                return ImgurUploadResult.builder()
                        .imageUrl(data.getLink())
                        .deleteHash(data.getDeletehash())
                        .build();
            }
            throw new RuntimeException("Failed to upload image to Imgur");
        } catch (Exception e) {
            log.error("Error uploading image to Imgur: ", e);
            throw new RuntimeException("Error uploading image: " + e.getMessage(), e);
        }
    }


    public void deleteImage(String deleteHash) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.add("Authorization", "Bearer " + accessToken);
            headers.add("Client-ID", clientId);

            HttpEntity<String> requestEntity = new HttpEntity<>(headers);

            // Используем deleteHash для удаления изображения
            String deleteUrl = "https://api.imgur.com/3/image/" + deleteHash;

            ResponseEntity<Void> response = restTemplate.exchange(
                    deleteUrl,
                    HttpMethod.DELETE,
                    requestEntity,
                    Void.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                log.info("Successfully deleted image with deleteHash: {}", deleteHash);
            } else {
                log.error("Failed to delete image. Status code: {}", response.getStatusCode());
                throw new RuntimeException("Failed to delete image from Imgur");
            }
        } catch (Exception e) {
            log.error("Error deleting image from Imgur: ", e);
            throw new RuntimeException("Error deleting image from Imgur: " + e.getMessage(), e);
        }
    }

    @Data
    public static class ImgurResponse {
        private ImgurData data;
        private boolean success;
        private int status;
    }


    @Data
    public static class ImgurData {
        private String id;
        private String title;
        private String description;
        private String link;
        private String deletehash;  // Изменено с deleteHash на deletehash
    }
    /**
     * Класс для хранения результата загрузки изображения
     */
    @Data
    @Builder
    public static class ImgurUploadResult {
        private String imageUrl;
        private String deleteHash;
    }
}