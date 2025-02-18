package com.example.askfm.enums;
import lombok.Getter;
import lombok.Setter;


@Getter
public enum JoinRequestStatus {
    PENDING("На рассмотрении"),
    APPROVED("Одобрено"),
    REJECTED("Отклонено"),
    CANCELLED("Отменено"); // если пользователь сам отменил заявку

    private final String displayName;

    JoinRequestStatus(String displayName) {
        this.displayName = displayName;
    }
}
