package com.example.askfm.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class StoryResponseDto {
    private Long id;
    private String username;
    private String userAvatar;
    private String imageUrl;
    private LocalDateTime createdAt;
    private int viewsCount;
    private List<ReactionCountDto> reactionCounts;
}
