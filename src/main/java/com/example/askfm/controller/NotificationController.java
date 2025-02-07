package com.example.askfm.controller;

import com.example.askfm.dto.NotificationDTO;
import com.example.askfm.model.User;
import com.example.askfm.service.NotificationService;
import com.example.askfm.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@Controller
@RequestMapping("/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {
    private final NotificationService notificationService;
    private final UserService userService;

    @GetMapping
    public String getNotifications(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        log.debug("📨 Получение страницы уведомлений для пользователя: {}", userDetails.getUsername());

        String username = userDetails.getUsername();
        User currentUser = userService.findByUsername(username);

        List<NotificationDTO> notifications = notificationService.getUserNotifications(userDetails.getUsername());
        long unreadCount = notificationService.getUnreadCount(userDetails.getUsername());


        model.addAttribute("notifications", notifications);
        model.addAttribute("unreadCount", unreadCount);
        model.addAttribute("currentUser", currentUser);

        return "notifications/notifications";
    }

    @PostMapping("/{id}/read")
    public String markAsRead(@PathVariable Long id,
                             @AuthenticationPrincipal UserDetails userDetails) {
        log.debug("✓ Отметка уведомления {} как прочитанного", id);
        notificationService.markAsRead(id, userDetails.getUsername());
        return "redirect:/notifications";
    }
    @PostMapping("/{id}/delete")
    public String deleteNotification(@PathVariable Long id,
                                     @AuthenticationPrincipal UserDetails userDetails) {
        log.debug("🗑️ Удаление уведомления {} пользователем {}",
                id, userDetails.getUsername());
        notificationService.deleteNotification(id, userDetails.getUsername());
        return "redirect:/notifications";
    }

    @PostMapping("/deleteNotifications")
    public String deleteNotifications(@AuthenticationPrincipal UserDetails userDetails) {
        notificationService.deleteAllUserNotifications(userDetails.getUsername());
        return "redirect:/notifications";
    }
}