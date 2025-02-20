package com.example.askfm.controller;

import com.example.askfm.dto.UserLockDTO;
import com.example.askfm.enums.UserRole;
import com.example.askfm.model.User;
import com.example.askfm.service.AdService;
import com.example.askfm.service.NewsService;
import com.example.askfm.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminDashboardController {
    private final UserService userService;




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

    @GetMapping("/settings")
    public String showSettings(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.findByUsername(userDetails.getUsername());
        if (currentUser.getRole() != UserRole.ADMIN) {
            return "redirect:/login";
        }


        return "admin/settings";
    }

    @GetMapping("/search-user")
    public String searchUser(@RequestParam String username, Model model) {
        User users = userService.findByUsername(username);
        model.addAttribute("users", users);
        return "admin/settings";
    }

    @PostMapping("/users/{username}/lock")
    @ResponseBody
    public ResponseEntity<?> lockUser(@PathVariable String username,
                                      @RequestBody UserLockDTO lockDTO) {
        try {
            userService.lockUser(username, lockDTO.getReason());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/users/{username}/unlock")
    @ResponseBody
    public ResponseEntity<?> unlockUser(@PathVariable String username) {
        try {
            userService.unlockUser(username);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
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


}