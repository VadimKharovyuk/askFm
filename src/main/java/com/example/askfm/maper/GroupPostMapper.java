package com.example.askfm.maper;

import com.example.askfm.dto.CreateGroupPostDTO;
import com.example.askfm.dto.GroupPostDTO;
import com.example.askfm.dto.GroupPostUserDTO;
import com.example.askfm.dto.UserDTO;
import com.example.askfm.model.Group;
import com.example.askfm.model.GroupPost;
import com.example.askfm.model.User;
import com.example.askfm.service.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashSet;

@Component
@RequiredArgsConstructor
public class GroupPostMapper {
    private final ImageService imageService;

    public GroupPost toEntity(CreateGroupPostDTO dto, Group group, User author) throws IOException {
        byte[] processedMedia = null;
        if (dto.getMedia() != null && !dto.getMedia().isEmpty()) {
            processedMedia = imageService.resizeImage(dto.getMedia().getBytes(), 1024);
        }

        return GroupPost.builder()
                .group(group)
                .author(author)
                .content(dto.getContent())
                .media(processedMedia)
                .isAnonymous(dto.isAnonymous())
                .publishedAt(LocalDateTime.now())
                .likedBy(new HashSet<>())
                .comments(new HashSet<>())
                .mentionedUsers(new HashSet<>())
                .build();
    }

    public GroupPostDTO toDto(GroupPost post, User currentUser) {
        return GroupPostDTO.builder()
                .id(post.getId())
                .groupId(post.getGroup().getId())
                .content(post.getContent())
                .mediaBase64(post.getMedia() != null ?
                        imageService.getBase64Avatar(post.getMedia()) : null)
                .publishedAt(post.getPublishedAt())
                .isAnonymous(post.isAnonymous())
                .author(post.isAnonymous() ? null : mapUser(post.getAuthor()))
                .likesCount(post.getLikedBy().size())
                .commentsCount(post.getComments().size())
                .isLikedByCurrentUser(post.getLikedBy().contains(currentUser))
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