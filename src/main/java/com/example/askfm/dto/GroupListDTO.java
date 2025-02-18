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
public class GroupListDTO {
    private Long id;
    private String name;
    private String description;
    private boolean isPrivate;
    private Integer membersCount;
    private Integer postsCount;
    private LocalDateTime createdAt;
    private String avatarBase64;
    private boolean isMember;
}
