package com.example.askfm.enums;

import lombok.Getter;

@Getter
public enum MembershipStatus {
    NOT_MEMBER,      // Не участник
    PENDING,         // Ожидает подтверждения
    MEMBER,          // Участник
    BANNED          // Заблокирован
}
