package com.example.askfm.enums;

import lombok.Getter;

@Getter
public enum UserRole {
    USER("User"),
    MODERATOR("Moderator"),
    ADMIN("Administrator");

    private final String displayName;

    UserRole(String displayName) {
        this.displayName = displayName;
    }

}
