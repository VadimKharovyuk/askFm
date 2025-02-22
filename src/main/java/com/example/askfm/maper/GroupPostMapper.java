package com.example.askfm.maper;

import com.example.askfm.dto.CreateGroupPostDTO;
import com.example.askfm.dto.GroupPostDTO;
import com.example.askfm.dto.GroupPostUserDTO;
import com.example.askfm.dto.UserDTO;
import com.example.askfm.model.Group;
import com.example.askfm.model.GroupPost;
import com.example.askfm.model.User;
import com.example.askfm.service.ImageService;
import com.example.askfm.service.ImgurStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashSet;
@Slf4j
@Component
@RequiredArgsConstructor
public class GroupPostMapper {
    private final ImageService imageService;
    private final ImgurStorageService imgurStorageService;

    public GroupPost toEntity(CreateGroupPostDTO dto, Group group, User author) throws IOException {
        // Загружаем медиа в Imgur если оно есть
        ImgurStorageService.ImgurUploadResult uploadResult = null;
        if (dto.getMedia() != null && !dto.getMedia().isEmpty()) {
            try {
                uploadResult = imgurStorageService.saveImage(dto.getMedia().getBytes());
                log.info("Successfully uploaded media to Imgur: {}", uploadResult.getImageUrl());
                log.info("Mapping media - URL: {}, DeleteHash: {}",
                        uploadResult.getImageUrl(),
                        uploadResult.getDeleteHash());
            } catch (Exception e) {
                log.error("Failed to upload media to Imgur", e);
                throw new IOException("Failed to process media", e);
            }
        }

        return GroupPost.builder()
                .group(group)
                .author(author)
                .content(dto.getContent())
                .mediaUrl(uploadResult != null ? uploadResult.getImageUrl() : null)
                .mediaDeleteHash(uploadResult != null ? uploadResult.getDeleteHash() : null)
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
                .mediaUrl(post.getMediaUrl()) // Теперь передаем URL напрямую
                .publishedAt(post.getPublishedAt())
                .mediaDeleteHash(post.getMediaDeleteHash())
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