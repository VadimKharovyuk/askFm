package com.example.askfm.service;

import com.example.askfm.model.Event;
import com.example.askfm.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class EventReminderScheduler {
    private final EventReminderService reminderService;
    private final EventRepository eventRepository;

    @Scheduled(cron = "0 0 0 * * *")
    public void scheduledReminderProcessing() {
        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime tomorrow = now.plusDays(1);

            List<Event> upcomingEvents = eventRepository.findByEventDateBetween(
                    tomorrow.withHour(0).withMinute(0),
                    tomorrow.withHour(23).withMinute(59)
            );

            log.info("Found {} upcoming events for tomorrow", upcomingEvents.size());

            reminderService.createRemindersForEvents(upcomingEvents);
            reminderService.processReminders();
            reminderService.deleteOldReminders(now.minusDays(7));

            log.info("Reminder processing completed successfully");
        } catch (Exception e) {
            log.error("Error during reminder processing: {}", e.getMessage(), e);
        }
    }
}