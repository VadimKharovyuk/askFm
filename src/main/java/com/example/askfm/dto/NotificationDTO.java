package com.example.askfm.dto;

import com.example.askfm.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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
    private String initiatorUsername; // имя пользователя, который вызвал уведомление
    private Long postId; // опционально, для лайков и комментариев
    private String postContent; // опционально, краткое содержание поста
    // можно добавить initiatorAvatar, если нужно отображать аватар пользователя
}
