package com.example.askfm.enums;

import lombok.Getter;


@Getter
public enum NotificationType {
    LIKE("поставил(-а) лайк вашему посту"),
    COMMENT("прокомментировал(-а) ваш пост"),
    REPOST("сделал репост вашего поста"),
    PHOTO_PURCHASE("купил(-а) вашу фотографию"),
    SUBSCRIPTION("подписался(-ась) на вас"),

    EVENT_CREATED("создал(-а) новое событие"),
    EVENT_UPDATED("обновил(-а) информацию о событии"),
    EVENT_CANCELLED("отменил(-а) событие"),
    EVENT_GOING("собирается посетить ваше событие"),
    EVENT_INTERESTED("заинтересовался(-ась) вашим событием");


    private final String actionMessage;

    NotificationType(String actionMessage) {
        this.actionMessage = actionMessage;
    }


}