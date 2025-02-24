package com.example.askfm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class PostDTO {
    private Long id;
    private String authorUsername;
    private String content;
    private String base64Media;
    private LocalDateTime publishedAt;
    private Long views;
    private int likesCount;
    private String authorAvatar;
    private boolean isLikedByCurrentUser;

    private Long commentsCount;
    private int repostsCount;
    private boolean isSavedByCurrentUser;

    private Set<String> tags;

    private String repostedBy;
    private LocalDateTime repostedAt;
    private String originalAuthorUsername;
    private LocalDateTime originalPublishedAt;


}
