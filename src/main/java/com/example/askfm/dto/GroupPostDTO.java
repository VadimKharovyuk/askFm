package com.example.askfm.dto;

import com.example.askfm.model.GroupMember;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
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