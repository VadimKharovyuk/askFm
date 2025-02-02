package com.example.askfm.enums;

import lombok.Getter;

@Getter
public enum NewsletterStatus {
    DRAFT("Черновик"),
    SCHEDULED("Запланировано"),
    SENT("Отправлено"),
    CANCELLED("Отменено");

    private final String displayName;

    NewsletterStatus(String displayName) {
        this.displayName = displayName;
    }
}