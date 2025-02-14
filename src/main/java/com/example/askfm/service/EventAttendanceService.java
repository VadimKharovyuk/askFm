package com.example.askfm.service;

import com.example.askfm.enums.ParticipationType;
import com.example.askfm.exception.EventNotFoundException;
import com.example.askfm.exception.UserNotFoundException;
import com.example.askfm.model.Event;
import com.example.askfm.model.EventAttendance;
import com.example.askfm.model.User;
import com.example.askfm.repository.EventAttendanceRepository;
import com.example.askfm.repository.EventRepository;
import com.example.askfm.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
@Slf4j
@Service
@RequiredArgsConstructor
public class EventAttendanceService {
    private final EventAttendanceRepository attendanceRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;


    @Transactional
    public void attendEvent(Long eventId, String username, ParticipationType status) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Event not found with id: " + eventId));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found with username: " + username));

        // Проверяем, не является ли пользователь создателем события
        if (event.getCreator().getUsername().equals(username)) {
            throw new IllegalStateException("Creator cannot attend their own event");
        }

        // Ищем существующую запись об участии
        EventAttendance attendance = attendanceRepository
                .findByEventIdAndUserUsername(eventId, username)
                .orElse(EventAttendance.builder()
                        .event(event)
                        .user(user)
                        .registeredAt(LocalDateTime.now())
                        .build());

        // Обновляем статус и время обновления
        attendance.setStatus(status);
        attendance.setUpdatedAt(LocalDateTime.now());

        EventAttendance savedAttendance = attendanceRepository.save(attendance);
        attendanceRepository.flush();
    }

    public void cancelAttendance(Long eventId, String username) {
        EventAttendance attendance = attendanceRepository
                .findByEventIdAndUserUsername(eventId, username)
                .orElseThrow(() -> new IllegalStateException("Attendance record not found"));

        attendanceRepository.delete(attendance);
    }

    public ParticipationType getUserParticipationStatus(Long eventId, String username) {
        return attendanceRepository
                .findByEventIdAndUserUsername(eventId, username)
                .map(EventAttendance::getStatus)
                .orElse(null);
    }
}
