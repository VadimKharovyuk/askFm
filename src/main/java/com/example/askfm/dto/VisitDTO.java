package com.example.askfm.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class VisitDTO {
    private Long visitorId;
    private String visitorUsername;
    private String visitorAvatar;
    private LocalDateTime visitedAt;
}
