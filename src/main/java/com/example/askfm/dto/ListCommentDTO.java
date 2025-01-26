package com.example.askfm.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ListCommentDTO {
    private Long id;
    private String authorUsername;
    private String content;
    private LocalDateTime createdAt;
    private String authorAvatar;
}
