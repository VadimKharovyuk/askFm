package com.example.askfm.enums;

import lombok.Getter;

@Getter
public enum EventStatus {
    ACTIVE("Активное"),        // Событие активно
    CANCELLED("Отменено"),     // Событие отменено
    COMPLETED("Завершено");    // Событие завершено (прошло)

    private final String displayName;

    EventStatus(String displayName) {
        this.displayName = displayName;
    }
}