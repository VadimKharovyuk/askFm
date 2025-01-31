package com.example.askfm.dto;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class AdResponseDto {
    private Long id;
    private String title;
    private String content;
    private String imageUrl;
    private String targetUrl;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private int impressions;
    private int clickCount;
    private BigDecimal totalBudget;
    private BigDecimal remainingBudget;
}