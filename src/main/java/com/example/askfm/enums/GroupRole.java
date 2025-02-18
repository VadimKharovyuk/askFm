package com.example.askfm.enums;

import lombok.Getter;

@Getter
public enum GroupRole {
    OWNER("Владелец"),
    ADMIN("Администратор"),
    MODERATOR("Модератор"),
    MEMBER("Участник");
    private final String displayName;
    GroupRole(String displayName) {
        this.displayName = displayName;
    }


    public boolean canApproveMembers() {
        return this == OWNER || this == ADMIN || this == MODERATOR;
    }
}