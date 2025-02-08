package com.example.askfm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PhotoDTO {
    private Long id;
    private Long ownerId;
    private String ownerUsername;
    private String photoBase64;
    private BigDecimal price;
    private Boolean isLocked;
    private String description;
    private LocalDateTime createdAt;
    private Boolean isNSFW;

}