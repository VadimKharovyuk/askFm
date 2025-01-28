package com.example.askfm.controller;
import com.example.askfm.dto.UpcomingBirthdayDTO;
import com.example.askfm.service.BirthdayService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/birthdays")
@RequiredArgsConstructor
public class BirthdayController {
    private final BirthdayService birthdayService;

    /**
     * Главная страница с днями рождения
     */
    @GetMapping
    public String getBirthdaysPage(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        String username = userDetails.getUsername();

        // Получаем дни рождения на сегодня
        List<UpcomingBirthdayDTO> todaysBirthdays = birthdayService.getTodaysBirthdays(username);
        model.addAttribute("todaysBirthdays", todaysBirthdays);

        // Получаем ближайшие дни рождения (на 30 дней вперед)
        List<UpcomingBirthdayDTO> upcomingBirthdays = birthdayService.getUpcomingBirthdays(username, 30);
        model.addAttribute("upcomingBirthdays", upcomingBirthdays);

        return "birthdays/index";
    }

    /**
     * Страница со всеми предстоящими днями рождения
     */
    @GetMapping("/upcoming")
    public String getUpcomingBirthdays(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "90") int days,
            Model model) {
        String username = userDetails.getUsername();
        List<UpcomingBirthdayDTO> birthdays = birthdayService.getUpcomingBirthdays(username, days);
        model.addAttribute("birthdays", birthdays);
        model.addAttribute("daysAhead", days);

        return "birthdays/upcoming";
    }

    /**
     * Страница с днями рождения текущего месяца
     */
    @GetMapping("/month")
    public String getMonthBirthdays(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        String username = userDetails.getUsername();
        List<UpcomingBirthdayDTO> birthdays = birthdayService.getBirthdaysThisMonth(username);
        model.addAttribute("birthdays", birthdays);

        return "birthdays/month";
    }
}