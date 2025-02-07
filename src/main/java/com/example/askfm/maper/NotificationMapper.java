package com.example.askfm.maper;

import com.example.askfm.dto.NotificationDTO;
import com.example.askfm.model.Notification;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class NotificationMapper {

    public NotificationDTO toDto(Notification notification) {
        return NotificationDTO.builder()
                .id(notification.getId())
                .type(notification.getType())
                .message(notification.getMessage())
                .createdAt(notification.getCreatedAt())
                .isRead(notification.isRead())
                .initiatorUsername(notification.getInitiator() != null ?
                        notification.getInitiator().getUsername() : null)
                .postId(notification.getPost() != null ? notification.getPost().getId() : null)
                .postContent(notification.getPost() != null ?
                        truncateContent(notification.getPost().getContent()) : null)
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