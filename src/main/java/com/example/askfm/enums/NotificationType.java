package com.example.askfm.enums;

import lombok.Getter;

@Getter
public enum NotificationType {
    LIKE("поставил(-а) лайк вашему посту"),
    COMMENT("прокомментировал(-а) ваш пост"),
    REPOST("сделал репост вашего поста"),
    SUBSCRIPTION("подписался(-ась) на вас");

    private final String actionMessage;

    NotificationType(String actionMessage) {
        this.actionMessage = actionMessage;
    }


}