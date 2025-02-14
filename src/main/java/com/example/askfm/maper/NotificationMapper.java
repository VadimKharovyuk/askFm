package com.example.askfm.maper;

import com.example.askfm.dto.EventCreateDto;
import com.example.askfm.dto.NotificationDTO;
import com.example.askfm.enums.EventStatus;
import com.example.askfm.enums.NotificationType;
import com.example.askfm.exception.EventCreationException;
import com.example.askfm.model.*;
import com.example.askfm.service.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class NotificationMapper {
    private final ImageService imageService;
    public  NotificationDTO toDto(Notification notification) {
        Post post = notification.getPost();
        Photo photo = notification.getPhoto();
        Event event = notification.getEvent();

        NotificationDTO.NotificationDTOBuilder builder = NotificationDTO.builder()
                .id(notification.getId())
                .type(notification.getType())
                .message(notification.getMessage())
                .createdAt(notification.getCreatedAt())
                .isRead(notification.isRead())
                .initiatorUsername(notification.getInitiator().getUsername())
                .initiatorAvatar(notification.getInitiator().getAvatar() != null ?
                        imageService.getBase64Avatar(notification.getInitiator().getAvatar()) : null);

        // Если это уведомление об отмене события, не пытаемся получить данные события
        if (notification.getType() == NotificationType.EVENT_CANCELLED) {
            return builder
                    .eventId(null)  // ID удаленного события
                    .eventTitle("Событие удалено")  // Заглушка для удаленного события
                    .build();
        }

        // Для остальных типов уведомлений добавляем информацию как обычно
        if (post != null) {
            builder
                    .postId(post.getId())
                    .postContent(post.getContent())
                    .postCreatedAt(post.getPublishedAt())
                    .postAuthorUsername(post.getAuthor().getUsername())
                    .postMedia(post.getMedia() != null ?
                            imageService.getBase64Avatar(post.getMedia()) : null);
        }

        if (photo != null) {
            builder
                    .photoId(photo.getId())
                    .photoDescription(photo.getDescription())
                    .photoPrice(photo.getPrice())
                    .photoBase64(photo.getPhoto() != null ?
                            imageService.getBase64Avatar(photo.getPhoto()) : null)
                    .photoOwnerUsername(photo.getOwner().getUsername());
        }

        // Обработка информации о событии
        if (notification.getType() == NotificationType.EVENT_CANCELLED) {
            // Для отмененных событий устанавливаем базовую информацию
            builder
                    .eventId(null)
                    .eventTitle("Событие удалено")
                    .eventDescription("Это событие больше недоступно")
                    .eventDate(null)
                    .eventCity(null)
                    .eventAddress(null)
                    .eventCreatorUsername(notification.getInitiator().getUsername())
                    .eventMedia(null)
                    .eventStatus(EventStatus.CANCELLED);
        } else if (event != null) {
            // Для всех остальных случаев обрабатываем как обычно
            builder
                    .eventId(event.getId())
                    .eventTitle(event.getTitle())
                    .eventDescription(event.getDescription())
                    .eventDate(event.getEventDate())
                    .eventCity(event.getCity())
                    .eventAddress(event.getAddress())
                    .eventCreatorUsername(event.getCreator().getUsername())
                    .eventMedia(event.getPhoto() != null ?
                            imageService.getBase64Avatar(event.getPhoto()) : null)
                    .eventStatus(event.getStatus());
        }

        return builder.build();
    }

    public Event toEntity(EventCreateDto dto, User creator) {
        return Event.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .eventDate(dto.getEventDate())
                .city(dto.getCity())
                .address(dto.getAddress())
                .creator(creator)
                .status(EventStatus.ACTIVE)  // Начальный статус
                .photo(dto.getPhoto() != null ? processPhoto(dto.getPhoto()) : null)  // Обработка фото
                .attendances(new ArrayList<>())  // Пустой список посещений
                .build();
    }

    // Метод для обработки фото
    private byte[] processPhoto(MultipartFile photo) {
        try {
            return photo.getBytes();
        } catch (IOException e) {
            throw new EventCreationException("Error processing event photo: " + e.getMessage());
        }
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