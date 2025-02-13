package com.example.askfm.EventListener;

import com.example.askfm.enums.NotificationType;
import com.example.askfm.model.Event;
import com.example.askfm.model.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class EventEvent {
    private Long eventId;  // Передаем только ID события
    private Long creatorId;  // Передаем только ID создателя
    private NotificationType type;

    public static EventEvent createEvent(Event savedEvent, User creator) {
        return new EventEvent(
                savedEvent.getId(),
                creator.getId(),
                NotificationType.EVENT_CREATED
        );
    }

}
