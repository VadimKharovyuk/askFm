package com.example.askfm.dto;

import com.example.askfm.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserSearchDTO {
    private Long id;
    private String username;
    private String email;
    private UserRole role;
    private String avatar;
    private String bio;
    private long followersCount;
    private long messageCount;
    private boolean isBlocked;
    private LocalDateTime lastActivity;
    private LocalDateTime createdAt;
}