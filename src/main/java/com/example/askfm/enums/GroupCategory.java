package com.example.askfm.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
public enum GroupCategory {
    EDUCATION("Освіта"),
    ENTERTAINMENT("Розваги"),
    BUSINESS("Бізнес"),
    TECHNOLOGY("Технології"),
    SPORTS("Спорт"),
    ART("Мистецтво"),
    MUSIC("Музика"),
    GAMING("Ігри"),
    SCIENCE("Наука"),
    CULTURE("Культура"),
    FOOD("Кулінарія"),
    POLITICS("Політика"),
    DEVELOPMENT("Розробка"),
    HEALTH("Здоров'я"),
    TRAVEL("Подорожі"),
    FASHION("Мода"),
    LITERATURE("Література"),
    PHOTOGRAPHY("Фотографія"),
    PETS("Домашні тварини"),
    OTHER("Інше");

    private final String displayName;
    GroupCategory(String displayName) {
        this.displayName = displayName;
    }
}