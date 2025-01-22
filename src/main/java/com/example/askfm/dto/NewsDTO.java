package com.example.askfm.dto;

import com.example.askfm.enums.NewsCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewsDTO {
    private Long id;
    private String title;
    private String content;
    private NewsCategory category;
    private Long viewCount;
    private MultipartFile thumbnail;
    private String thumbnailBase64;
    private String authorUsername;
    private LocalDateTime createdAt;
    private boolean published;
}
