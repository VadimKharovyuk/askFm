package com.example.askfm.enums;

import lombok.Getter;

@Getter
public enum EventCategory {
    MUSIC("Музика"),
    SPORTS("Спорт"),
    EDUCATION("Освіта"),
    NETWORKING("Нетворкінг"),
    ARTS("Мистецтво"),
    FOOD("Їжа"),
    BUSINESS("Бізнес"),
    TECHNOLOGY("Технології"),
    OTHER("Інше");

    private final String displayName;

    EventCategory(String displayName) {
        this.displayName = displayName;
    }
}
