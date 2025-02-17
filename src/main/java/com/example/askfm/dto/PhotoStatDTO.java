package com.example.askfm.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class PhotoStatDTO {
    private Long userId;
    private String username;
    private String userAvatar;
    private long totalPhotos;
    private BigDecimal totalEarnings;
    private long totalUnlocks;
}