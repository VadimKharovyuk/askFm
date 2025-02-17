package com.example.askfm.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class PhotoUnlockStatDTO {
    private Long photoId;
    private String ownerUsername;
    private String description;
    private BigDecimal price;
    private long unlockCount;
    private LocalDateTime createdAt;
}
