package com.example.askfm.enums;

import lombok.Getter;
@Getter
public enum ReactionType {
    LIKE("👍"),  // Лайк
    HEART("❤️"), // Сердце
    LAUGH("😂"), // Смешно
    SAD("😢"),   // Грусть
    WOW("😲"),   // Вау
    ANGRY("😡"); // Злой

    private final String emoji;

    ReactionType(String emoji) {
        this.emoji = emoji;
    }

    public String getEmoji() {
        return emoji;
    }
}
