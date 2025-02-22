package com.example.askfm.dto;

import com.example.askfm.enums.ReactionType;
import lombok.Builder;
import lombok.Data;
@Data
@Builder
public class ReactionCountDto {
    private ReactionType type;
    private String emoji;
    private long count;
}
