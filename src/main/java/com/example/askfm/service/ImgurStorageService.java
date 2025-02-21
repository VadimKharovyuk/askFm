package com.example.askfm.service;

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

    // Модифицируем метод для возврата всех данных об изображении
    public ImgurData uploadImage(byte[] imageData) {
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

            if (response.getBody() != null && response.getBody().getData() != null) {
                log.info("Image uploaded successfully to Imgur");
                return response.getBody().getData();
            }
            throw new RuntimeException("Failed to upload image to Imgur");
        } catch (Exception e) {
            log.error("Error uploading image to Imgur: ", e);
            throw new RuntimeException("Error uploading image to Imgur: " + e.getMessage(), e);
        }
    }

    // Метод для получения только URL
    public String uploadImageAndGetUrl(byte[] imageData) {
        return uploadImage(imageData).getLink();
    }

    public void deleteImage(String deleteHash) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.add("Authorization", "Bearer " + accessToken);
            headers.add("Client-ID", clientId);

            HttpEntity<String> requestEntity = new HttpEntity<>(headers);

            restTemplate.exchange(
                    "https://api.imgur.com/3/image/" + deleteHash,
                    HttpMethod.DELETE,
                    requestEntity,
                    Void.class
            );
            log.info("Image successfully deleted from Imgur");
        } catch (Exception e) {
            log.error("Error deleting image from Imgur: ", e);
            throw new RuntimeException("Error deleting image from Imgur: " + e.getMessage(), e);
        }
    }

    // Проверка валидности URL
    public boolean isImgurUrl(String url) {
        return url != null && url.startsWith("https://i.imgur.com/");
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
        private String link;        // URL изображения
        private String deleteHash;  // Хеш для удаления
    }
}