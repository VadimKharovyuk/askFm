package com.example.askfm.maper;

import com.example.askfm.dto.PostDTO;
import com.example.askfm.model.Post;
import com.example.askfm.repository.CommentRepository;
import com.example.askfm.repository.SavedPostRepository;
import com.example.askfm.service.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class PostMapper {
    private final ImageService imageService;
    private final CommentRepository commentRepository;
    private final SavedPostRepository savedPostRepository;

    public PostDTO toDto(Post post, String currentUsername) {
        if (post == null) {
            return null;
        }

        return PostDTO.builder()
                .id(post.getId())
                .authorUsername(post.getAuthor().getUsername())
                .authorAvatar(imageService.getBase64Avatar(post.getAuthor().getAvatar()))
                .content(post.getContent())
                .base64Media(post.getMedia() != null ?
                        imageService.getBase64Avatar(post.getMedia()) : null)
                .publishedAt(post.getPublishedAt())
                .views(post.getViews() != null ? (long) post.getViews().size() : 0L)
                .tags(new HashSet<>(post.getTags().stream()
                        .map(tag -> tag.getName())
                        .collect(Collectors.toSet())))
                .likesCount(post.getLikedBy().size())
                .isLikedByCurrentUser(currentUsername != null &&
                        post.getLikedBy().stream()
                                .anyMatch(user -> user.getUsername().equals(currentUsername)))
                .commentsCount(commentRepository.countByPostId(post.getId()))
                .repostsCount(post.getReposts().size())
                .isSavedByCurrentUser(currentUsername != null &&
                        savedPostRepository.existsByPostIdAndUserUsername(post.getId(), currentUsername))
                .build();
    }

    public PostDTO toDtoWithRepost(Post post, String currentUsername, String repostedBy,
                                   LocalDateTime repostedAt) {
        PostDTO dto = toDto(post, currentUsername);
        if (dto != null) {
            dto.setRepostedBy(repostedBy);
            dto.setRepostedAt(repostedAt);
            dto.setOriginalAuthorUsername(post.getAuthor().getUsername());
            dto.setOriginalPublishedAt(post.getPublishedAt());
        }
        return dto;
    }

    public List<PostDTO> toDtoList(List<Post> posts, String currentUsername) {
        if (posts == null) {
            return List.of();
        }
        return posts.stream()
                .map(post -> toDto(post, currentUsername))
                .collect(Collectors.toList());
    }
}