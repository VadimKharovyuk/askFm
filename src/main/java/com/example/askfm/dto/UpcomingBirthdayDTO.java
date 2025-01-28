package com.example.askfm.dto;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public  class UpcomingBirthdayDTO {
    private String username;            // имя пользователя
    private LocalDate birthDate;        // дата рождения
    private long daysUntilBirthday;     // дней до дня рождения
    private int upcomingAge;            // сколько лет исполнится

    // Добавляем новые поля для фронтенда
    private String avatarUrl;           // ссылка на аватар пользователя
    private boolean isBirthdayToday;    // флаг дня рождения сегодня
    private String formattedDate;       // отформатированная дата (например, "27 января")
    private String userProfileUrl;      // ссылка на профиль пользователя

}
