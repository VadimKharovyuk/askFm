package com.example.askfm.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class SavedPostDTO {
    private Long id;
    private Long postId;
    private String authorUsername;
    private String content;
    private LocalDateTime savedAt;
    private LocalDateTime postPublishedAt;
}
