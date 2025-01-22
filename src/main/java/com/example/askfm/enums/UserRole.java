package com.example.askfm.enums;

public enum UserRole {
    USER("User"),
    MODERATOR("Moderator"),
    ADMIN("Administrator");

    private final String displayName;

    UserRole(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
