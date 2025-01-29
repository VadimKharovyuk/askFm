package com.example.askfm.enums;

import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public enum ReportCategory {
    SPAM("Спам"),
    INAPPROPRIATE("Неприемлемый контент"),      // Неприемлемый контент
    HARASSMENT("Домогательство"),         // Домогательство
    HATE_SPEECH("Язык вражды"),       // Язык вражды
    VIOLENCE("Насилие"),          // Насилие
    FALSE_INFORMATION("Ложная информация"), // Ложная информация
    COPYRIGHT("Нарушение авторских прав"),         // Нарушение авторских прав
    OTHER ("Другое")       ;     // Другое

    private final String displayName;

    ReportCategory(String displayName) {
        this.displayName = displayName;
    }
}