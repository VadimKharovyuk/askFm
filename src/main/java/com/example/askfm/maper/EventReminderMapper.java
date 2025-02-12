package com.example.askfm.maper;

import com.example.askfm.dto.EventReminderDto;
import com.example.askfm.model.EventReminder;
import org.springframework.stereotype.Component;

@Component
public class EventReminderMapper {

    public EventReminderDto toDto(EventReminder reminder) {
        if (reminder == null) {
            return null;
        }

        return EventReminderDto.builder()
                .id(reminder.getId())
                .eventId(reminder.getEvent().getId())
                .eventTitle(reminder.getEvent().getTitle())
                .userId(reminder.getUser().getId())
                .username(reminder.getUser().getUsername())
                .sentAt(reminder.getSentAt())
                .sent(reminder.isSent())
                .build();
    }
}