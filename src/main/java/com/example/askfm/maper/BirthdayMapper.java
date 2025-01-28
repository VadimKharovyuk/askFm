package com.example.askfm.maper;
import com.example.askfm.dto.UpcomingBirthdayDTO;
import com.example.askfm.model.User;
import com.example.askfm.service.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Component
@RequiredArgsConstructor
public class BirthdayMapper {
    private final ImageService imageService;

    public UpcomingBirthdayDTO toUpcomingBirthdayDTO(User user, LocalDate today) {
        LocalDate birthDate = user.getProfile().getDateOfBirth();  // Изменено с getUserProfile на getProfile
        LocalDate nextBirthday = getNextBirthday(birthDate, today);
        long daysUntil = ChronoUnit.DAYS.between(today, nextBirthday);
        int upcomingAge = nextBirthday.getYear() - birthDate.getYear();

        // Форматируем дату на русском языке
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMMM", new Locale("ru"));
        String formattedDate = birthDate.format(formatter);

        String avatarUrl = null;
        if (user.getAvatar() != null) {  // Теперь берем аватар напрямую из User
            avatarUrl = imageService.getBase64Avatar(user.getAvatar());
        }

        return UpcomingBirthdayDTO.builder()
                .username(user.getUsername())
                .birthDate(birthDate)
                .daysUntilBirthday(daysUntil)
                .upcomingAge(upcomingAge)
                .avatarUrl(avatarUrl)
                .isBirthdayToday(isBirthdayToday(birthDate, today))
                .formattedDate(formattedDate)
                .userProfileUrl("/users/" + user.getUsername())
                .build();
    }

    private LocalDate getNextBirthday(LocalDate birthDate, LocalDate today) {
        LocalDate nextBirthday = birthDate.withYear(today.getYear());

        if (nextBirthday.isBefore(today) || nextBirthday.isEqual(today)) {
            nextBirthday = nextBirthday.plusYears(1);
        }

        return nextBirthday;
    }

    public boolean isBirthdayToday(LocalDate birthDate, LocalDate today) {
        return birthDate.getMonth() == today.getMonth() &&
                birthDate.getDayOfMonth() == today.getDayOfMonth();
    }
}