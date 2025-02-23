package com.example.askfm.dto;

import com.example.askfm.enums.ReactionType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
// DTO для реакции
@Data
@Builder
public class StoryReactionDto {
    private String username;
    private ReactionType reactionType;
    private String emoji;
    private LocalDateTime reactedAt;
    private String userAvatar;
}
