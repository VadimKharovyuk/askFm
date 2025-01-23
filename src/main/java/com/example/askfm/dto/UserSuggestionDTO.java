package com.example.askfm.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserSuggestionDTO {
    private String username;
    private String avatarBase64;
    private int subscribersCount;
}
