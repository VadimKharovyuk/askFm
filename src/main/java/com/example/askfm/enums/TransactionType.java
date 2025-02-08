package com.example.askfm.enums;

import lombok.Getter;

@Getter
public enum TransactionType {

    PHOTO_PURCHASE("покупка фото"),
    BALANCE_DEPOSIT("поповнення балансу"),
    BALANCE_WITHDRAWAL("виведення коштів");

    private final String description;

    TransactionType(String description) {
        this.description = description;
    }
}
