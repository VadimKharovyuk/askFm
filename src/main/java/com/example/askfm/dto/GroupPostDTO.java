package com.example.askfm.dto;

import com.example.askfm.model.GroupMember;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupPostDTO {
    private Long id;
    private Long groupId;
    private String content;
    private String mediaBase64;
    private LocalDateTime publishedAt;
    private boolean isAnonymous;
    private GroupPostUserDTO author;
    private int likesCount;
    private int commentsCount;
    private boolean isLikedByCurrentUser;

    
}