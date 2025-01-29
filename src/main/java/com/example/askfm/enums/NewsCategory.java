package com.example.askfm.enums;

import lombok.Getter;

@Getter
public enum NewsCategory {
    FEATURE_UPDATES("Обновления функционала"),
    SECURITY("Безопасность"),
    PLATFORM_NEWS("Новости платформы"),
    USER_GUIDES("Руководства пользователя"),
    COMMUNITY("Сообщество"),
    DEVELOPMENT("Разработка"),
    API_UPDATES("API обновления"),
    MAINTENANCE("Техническое обслуживание"),
    OTHER("Другое");

    private final String displayName;

    NewsCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
