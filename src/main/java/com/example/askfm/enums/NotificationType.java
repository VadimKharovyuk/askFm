package com.example.askfm.enums;

public enum NotificationType {
    LIKE("поставил(-а) лайк вашему посту"),
    COMMENT("прокомментировал(-а) ваш пост"),
    SUBSCRIPTION("подписался(-ась) на вас");

    private final String actionMessage;

    NotificationType(String actionMessage) {
        this.actionMessage = actionMessage;
    }

    public String getActionMessage() {
        return actionMessage;
    }
}