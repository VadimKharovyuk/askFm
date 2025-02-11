package com.example.askfm.enums;

import lombok.Getter;

@Getter
public enum ParticipationType {
    GOING("Йду"),
    INTERESTED("Цікавлюсь");

    private final String displayName;

    ParticipationType(String displayName) {
        this.displayName = displayName;
    }
}