package com.example.askfm.maper;

import com.example.askfm.dto.CreateCommentDTO;
import com.example.askfm.dto.GroupPostCommentDTO;
import com.example.askfm.dto.GroupPostUserDTO;
import com.example.askfm.model.GroupPost;
import com.example.askfm.model.GroupPostComment;
import com.example.askfm.model.User;
import com.example.askfm.service.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class GroupPostCommentMapper {
    private final ImageService imageService;

    public GroupPostComment toEntity(CreateCommentDTO dto, GroupPost post, User author) {
        return GroupPostComment.builder()
                .post(post)
                .author(author)
                .content(dto.getContent())
                .createdAt(LocalDateTime.now())
                .build();
    }

    public GroupPostCommentDTO toDto(GroupPostComment comment) {
        return GroupPostCommentDTO.builder()
                .id(comment.getId())
                .postId(comment.getPost().getId())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .author(mapUser(comment.getAuthor()))
                .build();
    }

    private GroupPostUserDTO mapUser(User user) {
        return GroupPostUserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .avatarBase64(user.getAvatar() != null ?
                        imageService.getBase64Avatar(user.getAvatar()) : null)
                .build();
    }
}