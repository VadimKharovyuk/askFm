package com.example.askfm.maper;

import com.example.askfm.dto.*;
import com.example.askfm.enums.ParticipationType;
import com.example.askfm.model.Event;
import com.example.askfm.model.EventAttendance;
import com.example.askfm.model.User;
import com.example.askfm.service.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class EventMapper {
    private final ImageService imageService;

    public Event toEntity(EventCreateDto dto, User creator) throws IOException {
        byte[] resizedPhoto = null;
        if (dto.getPhoto() != null && !dto.getPhoto().isEmpty()) {
            byte[] originalPhoto = dto.getPhoto().getBytes();
            resizedPhoto = imageService.resizeImage(originalPhoto, 800); // размер можно настроить
        }

        return Event.builder()
                .creator(creator)
                .title(dto.getTitle())
                .description(dto.getDescription())
                .photo(resizedPhoto)
                .location(dto.getLocation())
                .address(dto.getAddress())
                .city(dto.getCity())
                .category(dto.getCategory())
                .eventDate(dto.getEventDate())
                .createdAt(LocalDateTime.now())
                .price(dto.getPrice())
                .ageRestriction(dto.getAgeRestriction())
                .duration(dto.getDuration())
                .build();
    }
    public EventResponseDto toDto(Event event, ParticipationType userParticipationType) {
        int goingCount = 0;
        int interestedCount = 0;

        List<EventAttendance> attendances = event.getAttendances();
        if (attendances != null) {
            for (EventAttendance attendance : attendances) {
                if (attendance.getStatus() == ParticipationType.GOING) {
                    goingCount++;
                } else if (attendance.getStatus() == ParticipationType.INTERESTED) {
                    interestedCount++;
                }
            }
        }

        return EventResponseDto.builder()
                .id(event.getId())
                .creator(toUserShortDto(event.getCreator()))
                .title(event.getTitle())
                .description(event.getDescription())
                .photoBase64(imageService.getBase64Avatar(event.getPhoto()))
                .location(event.getLocation())
                .address(event.getAddress())
                .city(event.getCity())
                .category(event.getCategory())
                .eventDate(event.getEventDate())
                .createdAt(event.getCreatedAt())
                .price(event.getPrice())
                .ageRestriction(event.getAgeRestriction())
                .duration(event.getDuration())
                .goingCount(goingCount)
                .interestedCount(interestedCount)
                .userParticipationType(userParticipationType)
                .build();
    }

    public EventAttendanceDto toAttendanceDto(EventAttendance attendance) {
        return EventAttendanceDto.builder()
                .id(attendance.getId())
                .user(toUserShortDto(attendance.getUser()))
                .status(attendance.getStatus())
                .registeredAt(attendance.getRegisteredAt())
                .build();
    }

    private UserShortDto toUserShortDto(User user) {
        return UserShortDto.builder()
                .id(user.getId())
                .name(user.getUsername())
                .email(user.getEmail())
                .build();
    }
    public List<EventListDto> toListDto(List<Event> events) {
        return events.stream()
                .map(event -> {
                    int goingCount = 0;
                    int interestedCount = 0;

                    for (EventAttendance attendance : event.getAttendances()) {
                        if (attendance.getStatus() == ParticipationType.GOING) {
                            goingCount++;
                        } else if (attendance.getStatus() == ParticipationType.INTERESTED) {
                            interestedCount++;
                        }
                    }

                    return EventListDto.builder()
                            .id(event.getId())
                            .title(event.getTitle())
                            .photoBase64(imageService.getBase64Avatar(event.getPhoto()))
                            .eventDate(event.getEventDate())
                            .location(event.getLocation())
                            .address(event.getAddress())
                            .city(event.getCity())
                            .goingCount(goingCount)
                            .interestedCount(interestedCount)
                            .build();
                })
                .collect(Collectors.toList());
    }
}
