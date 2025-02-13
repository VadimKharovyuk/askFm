package com.example.askfm.maper;

import com.example.askfm.dto.EventCreateDto;
import com.example.askfm.dto.NotificationDTO;
import com.example.askfm.enums.EventStatus;
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

    public NotificationDTO toDto(Notification notification) {
        Post post = notification.getPost();
        Photo photo = notification.getPhoto();
        Event event = notification.getEvent();

        return NotificationDTO.builder()
                .id(notification.getId())
                .type(notification.getType())
                .message(notification.getMessage())
                .createdAt(notification.getCreatedAt())
                .isRead(notification.isRead())
                .initiatorUsername(notification.getInitiator().getUsername())
                .postId(post != null ? post.getId() : null)
                .initiatorAvatar(notification.getInitiator().getAvatar() != null ?
                        imageService.getBase64Avatar(notification.getInitiator().getAvatar()) : null)
                // Информация о посте
                .postContent(post != null ? post.getContent() : null)
                .postCreatedAt(post != null ? post.getPublishedAt() : null)
                .postAuthorUsername(post != null ? post.getAuthor().getUsername() : null)
                .postMedia(post != null && post.getMedia() != null ?
                        imageService.getBase64Avatar(post.getMedia()) : null)

                // Информация о фото
                .photoId(photo != null ? photo.getId() : null)
                .photoDescription(photo != null ? photo.getDescription() : null)
                .photoPrice(photo != null ? photo.getPrice() : null)
                .photoBase64(photo != null && photo.getPhoto() != null ?
                        imageService.getBase64Avatar(photo.getPhoto()) : null)
                .photoOwnerUsername(photo != null ? photo.getOwner().getUsername() : null)

                //инфа ивент
                .eventId(event != null ? event.getId() : null)
                .eventTitle(event != null ? event.getTitle() : null)
                .eventDescription(event != null ? event.getDescription() : null)
                .eventDate(event != null ? event.getEventDate() : null)
                .eventCity(event != null ? event.getCity() : null)
                .eventAddress(event != null ? event.getAddress() : null)
                .eventCreatorUsername(event != null ? event.getCreator().getUsername() : null)
                .eventMedia(event != null && event.getPhoto() != null ?
                        imageService.getBase64Avatar(event.getPhoto()) : null)
                .eventStatus(event != null ? event.getStatus() : null)

                .build();
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