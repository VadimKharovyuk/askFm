package com.example.askfm.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;


// DTO для просмотра
@Data
@Builder
public class StoryViewDto {
    private String username;
    private LocalDateTime viewedAt;
    private String userAvatar;
}