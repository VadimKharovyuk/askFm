package com.example.askfm.enums;

import lombok.Getter;

@Getter
public enum TicketStatus {
    NEW("New ticket"),
    OPEN("Open ticket"),
    IN_PROGRESS("In progress"),
    RESOLVED("Resolved"),
    CLOSED("Closed");

    private final String displayName;

    TicketStatus(String displayName) {
        this.displayName = displayName;
    }
}
