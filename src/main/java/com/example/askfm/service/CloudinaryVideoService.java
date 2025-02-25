package com.example.askfm.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;

@Slf4j
@Service
public class CloudinaryVideoService {

    @Value("${cloudinary.cloud-name}")
    private String cloudName;

    @Value("${cloudinary.api-key}")
    private String apiKey;

    @Value("${cloudinary.api-secret}")
    private String apiSecret;

    private Cloudinary cloudinary;

    @PostConstruct
    public void init() {
        cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret));
        log.info("Cloudinary initialized with cloud name: {}", cloudName);
    }

    public CloudinaryUploadResult uploadVideo(byte[] videoData, String fileName) {
        try {
            Map uploadResult = cloudinary.uploader().upload(videoData, ObjectUtils.asMap(
                    "resource_type", "video",
                    "public_id", fileName
            ));
            log.info("Video uploaded successfully to Cloudinary. Public ID: {}", fileName);

            return CloudinaryUploadResult.builder()
                    .videoUrl(uploadResult.get("secure_url").toString())
                    .publicId(uploadResult.get("public_id").toString())
                    .build();
        } catch (IOException e) {
            log.error("Error uploading video to Cloudinary: ", e);
            throw new RuntimeException("Error uploading video: " + e.getMessage(), e);
        }
    }

    public void deleteVideo(String publicId) {
        try {
            Map result = cloudinary.uploader().destroy(publicId, ObjectUtils.asMap(
                    "resource_type", "video"
            ));

            if (result.get("result").equals("ok")) {
                log.info("Successfully deleted video with public ID: {}", publicId);
            } else {
                log.error("Failed to delete video. Result: {}", result);
                throw new RuntimeException("Failed to delete video from Cloudinary");
            }
        } catch (IOException e) {
            log.error("Error deleting video from Cloudinary: ", e);
            throw new RuntimeException("Error deleting video: " + e.getMessage(), e);
        }
    }

    /**
     * Класс для хранения результата загрузки видео
     */
    @Data
    @Builder
    public static class CloudinaryUploadResult {
        private String videoUrl;
        private String publicId;
    }
}