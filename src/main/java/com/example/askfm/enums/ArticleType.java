package com.example.askfm.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
public enum ArticleType {
    TERMS_OF_USE("Условия использования"),
    PRIVACY_POLICY("Политика конфиденциальности");

    private final String displayName;

    ArticleType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
