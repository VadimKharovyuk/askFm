package com.example.askfm.maper;

import com.example.askfm.dto.NotificationDTO;
import com.example.askfm.model.Notification;
import com.example.askfm.model.Post;
import com.example.askfm.model.Tag;
import com.example.askfm.service.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class NotificationMapper {
    private final ImageService imageService;

    public NotificationDTO toDto(Notification notification) {
        Post post = notification.getPost();

        return NotificationDTO.builder()
                .id(notification.getId())
                .type(notification.getType())
                .message(notification.getMessage())
                .createdAt(notification.getCreatedAt())
                .isRead(notification.isRead())
                .initiatorUsername(notification.getInitiator().getUsername())
                .postId(post != null ? post.getId() : null)
                // Информация о посте
                .postContent(post != null ? post.getContent() : null)
//                .postCreatedAt(post != null ? post.getCreatedAt() : null)
                .postAuthorUsername(post != null ? post.getAuthor().getUsername() : null)
                .postMedia(post != null && post.getMedia() != null ?
                        imageService.getBase64Avatar(post.getMedia()) : null)
                .build();
    }

    public List<NotificationDTO> toDtoList(List<Notification> notifications) {
        return notifications.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private String truncateContent(String content) {
        if (content == null) return null;
        return content.length() > 50 ? content.substring(0, 47) + "..." : content;
    }
}