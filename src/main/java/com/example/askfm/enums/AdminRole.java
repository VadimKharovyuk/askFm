package com.example.askfm.enums;

public enum AdminRole {
    CONTENT_MANAGER("Контент-менеджер"),
    MODERATOR("Модератор"),
    SUPER_ADMIN("Супер-администратор");

    private final String displayName;

    AdminRole(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}