package com.example.askfm.dto;

import com.example.askfm.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDTO {
    private Long id;
    private NotificationType type;
    private String message;
    private LocalDateTime createdAt;
    private boolean isRead;
    private String initiatorUsername;
    private String initiatorAvatar;
    private Long postId;

    // Информация о посте
    private String postContent;
    private LocalDateTime postCreatedAt;
    private String postAuthorUsername;
    private String postMedia;

    private PostDTO post;
}
