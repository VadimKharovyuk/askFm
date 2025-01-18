package com.example.askfm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSearchDTO {
    private String username;
    private String avatar;
    private String bio;
    private long followersCount;
    private boolean isFollowing;
}