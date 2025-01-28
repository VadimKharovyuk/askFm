package com.example.askfm.service;

import com.example.askfm.dto.UpcomingBirthdayDTO;
import com.example.askfm.maper.BirthdayMapper;
import com.example.askfm.model.Subscription;
import com.example.askfm.model.User;
import com.example.askfm.repository.SubscriptionRepository;
import com.example.askfm.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class BirthdayService {
    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    private final BirthdayMapper birthdayMapper;

    /**
     * Получает список пользователей, у которых сегодня день рождения
     */
    public List<UpcomingBirthdayDTO> getTodaysBirthdays(String username) {
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден: " + username));

        LocalDate today = LocalDate.now();

        return subscriptionRepository.findBySubscriber(currentUser)
                .stream()
                .map(Subscription::getSubscribedTo)
                .filter(user -> user.getProfile() != null &&
                        user.getProfile().getDateOfBirth() != null &&
                        birthdayMapper.isBirthdayToday(user.getProfile().getDateOfBirth(), today))
                .map(user -> birthdayMapper.toUpcomingBirthdayDTO(user, today))
                .collect(Collectors.toList());
    }

    /**
     * Получает список предстоящих дней рождения на указанный период
     */
    public List<UpcomingBirthdayDTO> getUpcomingBirthdays(String username, int daysAhead) {
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден: " + username));

        LocalDate today = LocalDate.now();

        return subscriptionRepository.findBySubscriber(currentUser)
                .stream()
                .map(Subscription::getSubscribedTo)
                .filter(user -> user.getProfile() != null &&
                        user.getProfile().getDateOfBirth() != null)
                .map(user -> birthdayMapper.toUpcomingBirthdayDTO(user, today))
                .filter(dto -> dto.getDaysUntilBirthday() <= daysAhead)
                .sorted(Comparator.comparingLong(UpcomingBirthdayDTO::getDaysUntilBirthday))
                .collect(Collectors.toList());
    }

    /**
     * Получает ближайшие дни рождения (например, следующие 5)
     */
    public List<UpcomingBirthdayDTO> getNextBirthdays(String username, int count) {
        return getUpcomingBirthdays(username, 365) // Проверяем на год вперед
                .stream()
                .limit(count)
                .collect(Collectors.toList());
    }

    /**
     * Получает количество дней рождения на сегодня
     */
    public long getTodaysBirthdaysCount(String username) {
        return getTodaysBirthdays(username).size();
    }

    /**
     * Проверяет, есть ли сегодня у кого-то день рождения
     */
    public boolean hasBirthdaysToday(String username) {
        return getTodaysBirthdaysCount(username) > 0;
    }

    /**
     * Получает дни рождения на определенную дату
     */
    public List<UpcomingBirthdayDTO> getBirthdaysOnDate(String username, LocalDate date) {
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден: " + username));

        return subscriptionRepository.findBySubscriber(currentUser)
                .stream()
                .map(Subscription::getSubscribedTo)
                .filter(user -> user.getProfile() != null &&
                        user.getProfile().getDateOfBirth() != null &&
                        isBirthdayOnDate(user.getProfile().getDateOfBirth(), date))
                .map(user -> birthdayMapper.toUpcomingBirthdayDTO(user, LocalDate.now()))
                .collect(Collectors.toList());
    }

    /**
     * Получает дни рождения на текущий месяц
     */
    public List<UpcomingBirthdayDTO> getBirthdaysThisMonth(String username) {
        LocalDate today = LocalDate.now();
        LocalDate endOfMonth = today.withDayOfMonth(today.lengthOfMonth());
        return getUpcomingBirthdays(username, (int) ChronoUnit.DAYS.between(today, endOfMonth));
    }

    /**
     * Проверяет, день рождения ли у пользователя в указанную дату
     */
    private boolean isBirthdayOnDate(LocalDate birthDate, LocalDate date) {
        return birthDate.getMonth() == date.getMonth() &&
                birthDate.getDayOfMonth() == date.getDayOfMonth();
    }
}