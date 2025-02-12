package com.example.askfm.service;

import com.example.askfm.dto.EventReminderDto;
import com.example.askfm.maper.EventReminderMapper;
import com.example.askfm.model.Event;
import com.example.askfm.model.EventAttendance;
import com.example.askfm.model.EventReminder;
import com.example.askfm.model.User;
import com.example.askfm.repository.EventReminderRepository;
import com.example.askfm.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class EventReminderService {
    private final EventReminderRepository reminderRepository;
    private final EventReminderMapper reminderMapper;
    private final EmailService emailService;


    @Transactional
    public void createReminder(Event event, User user) {
        if (reminderRepository.existsByEventAndUserAndSent(event, user, true)) {
            log.info("Reminder already sent for event {} to user {}",
                    event.getId(), user.getId());
            return;
        }

        EventReminder reminder = new EventReminder();
        reminder.setEvent(event);
        reminder.setUser(user);
        reminder.setSentAt(LocalDateTime.now());
        reminder.setSent(false);

        reminderRepository.save(reminder);
    }

    @Transactional
    public void markAsSent(Long reminderId) {
        if (reminderId == null) {
            throw new IllegalArgumentException("ReminderId cannot be null");
        }
        reminderRepository.findById(reminderId)
                .ifPresentOrElse(
                        reminder -> {
                            reminder.setSent(true);
                            reminder.setSentAt(LocalDateTime.now());
                            reminderRepository.save(reminder);
                            log.info("Reminder {} marked as sent", reminderId);
                        },
                        () -> {
                            log.warn("Reminder {} not found", reminderId);
                            throw new RuntimeException("Reminder not found");
                        }
                );
    }

    @Transactional(readOnly = true)
    public List<EventReminderDto> getUnsentReminders() {
        return reminderRepository.findAll().stream()
                .filter(reminder -> !reminder.isSent())
                .map(reminderMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<EventReminderDto> getRemindersByEvent(Event event, LocalDateTime start, LocalDateTime end) {
        return reminderRepository.findByEventAndSentAtBetween(event, start, end).stream()
                .map(reminderMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void processReminders() {
        List<EventReminder> unsentReminders = reminderRepository.findAll().stream()
                .filter(reminder -> !reminder.isSent())
                .toList();

        for (EventReminder reminder : unsentReminders) {
            try {
                emailService.sendEventReminder(reminder.getUser(), reminder.getEvent());
                reminder.setSent(true);
                reminder.setSentAt(LocalDateTime.now());
                reminderRepository.save(reminder);

                log.info("Successfully sent reminder for event {} to user {}",
                        reminder.getEvent().getId(), reminder.getUser().getId());
            } catch (Exception e) {
                log.error("Failed to send reminder for event {} to user {}: {}",
                        reminder.getEvent().getId(), reminder.getUser().getId(),
                        e.getMessage());
            }
        }

    }

    @Transactional
    public void deleteOldReminders(LocalDateTime before) {
        if (before == null) {
            throw new IllegalArgumentException("Before date cannot be null");
        }

        List<EventReminder> oldReminders = reminderRepository.findAll().stream()
                .filter(reminder -> reminder.isSent() &&
                        reminder.getSentAt().isBefore(before))
                .collect(Collectors.toList());

        if (!oldReminders.isEmpty()) {
            reminderRepository.deleteAll(oldReminders);
            log.info("Deleted {} old reminders", oldReminders.size());
        }
    }

    @Transactional
    public void createRemindersForEvents(List<Event> events) {
        if (events == null) {
            log.warn("Events list is null");
            return;
        }

        for (Event event : events) {
            if (event == null || event.getAttendances() == null) {
                continue;
            }

            event.getAttendances().stream()
                    .map(EventAttendance::getUser)
                    .filter(Objects::nonNull)
                    .forEach(user -> {
                        try {
                            createReminder(event, user);
                        } catch (Exception e) {
                            log.error("Failed to create reminder for event {} and user {}: {}",
                                    event.getId(), user.getId(), e.getMessage());
                        }
                    });
        }
    }

    @Transactional(readOnly = true)
    public List<EventReminderDto> getSentReminders() {
        return reminderRepository.findAll().stream()
                .filter(EventReminder::isSent)
                .map(reminderMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<EventReminderDto> getAllReminders() {
        return reminderRepository.findAll().stream()
                .map(reminderMapper::toDto)
                .collect(Collectors.toList());
    }

    public void deleteAll() {
        reminderRepository.deleteAll();
    }
}