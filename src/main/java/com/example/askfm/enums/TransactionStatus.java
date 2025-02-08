package com.example.askfm.enums;

import lombok.Getter;

@Getter
public enum TransactionStatus {
    PENDING("в очікуванні"),
    COMPLETED("завершено"),
    FAILED("не вдалося");

    public final String description;

    TransactionStatus(String description) {
        this.description = description;
    }
}
