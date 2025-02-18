package com.example.askfm.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
public enum GroupCategory {
    EDUCATION("Образование"),
    ENTERTAINMENT("Развлечения"),
    BUSINESS("Бизнес"),
    TECHNOLOGY("Технологии"),
    SPORTS("Спорт"),
    ART("Искусство"),
    MUSIC("Музыка"),
    GAMING("Игры"),
    OTHER("Другое");


    private final String displayName;
    GroupCategory(String displayName) {
        this.displayName = displayName;
    }
}
