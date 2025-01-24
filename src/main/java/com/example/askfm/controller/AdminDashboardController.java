package com.example.askfm.controller;

import com.example.askfm.enums.UserRole;
import com.example.askfm.model.User;
import com.example.askfm.service.NewsService;
import com.example.askfm.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminDashboardController {
    private final UserService userService;
//    private final NewsService newsService;

    @GetMapping("/dashboard")
    public String showDashboard(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        // Получаем пользователя и проверяем его роль
        User currentUser = userService.findByUsername(userDetails.getUsername());
        if (currentUser.getRole() != UserRole.ADMIN) {
            return "redirect:/login";
        }

        // Данные для дашборда
        model.addAttribute("totalUsers", userService.getTotalUsersCount());
        model.addAttribute("newRegistrations", userService.getNewRegistrationsGrowth());
        model.addAttribute("engagementRate", userService.getEngagementRate());
        model.addAttribute("adminUsername", currentUser.getUsername());

        // Статистика новостей
//        model.addAttribute("totalNews", newsService.getTotalNewsCount());
//        model.addAttribute("recentNews", newsService.getRecentNews());
//        model.addAttribute("totalViews", newsService.getTotalViewsCount());
//        model.addAttribute("totalComments", newsService.getTotalCommentsCount());

        return "admin/dashboard";
    }

    @GetMapping("/analytics")
    public String showAnalytics(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.findByUsername(userDetails.getUsername());
        if (currentUser.getRole() != UserRole.ADMIN) {
            return "redirect:/login";
        }
        // Добавить данные для аналитики
        return "admin/analytics";
    }

    @GetMapping("/settings")
    public String showSettings(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.findByUsername(userDetails.getUsername());
        if (currentUser.getRole() != UserRole.ADMIN) {
            return "redirect:/login";
        }
        // Добавить настройки
        return "admin/settings";
    }
}