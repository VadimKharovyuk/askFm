package com.example.askfm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BlockInfoDTO {
    private Long id;
    private String blockerUsername;
    private String blockedUsername;
    private LocalDateTime blockedAt;
}