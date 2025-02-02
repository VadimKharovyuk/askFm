package com.example.askfm.controller;

import com.example.askfm.enums.UserRole;
import com.example.askfm.model.User;
import com.example.askfm.service.AdService;
import com.example.askfm.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

@Controller
@RequestMapping("/admin/users")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class AdminCoinController {
    private final UserService userService;
    public final AdService adService;

    @GetMapping("/add-coins")
    public String showAddCoinsPage(Model model ,@AuthenticationPrincipal UserDetails userDetails) {

        // Проверка на null перед использованием
        if (userDetails == null) {
            return "redirect:/login";
        }
        try {
            // Безопасное получение имени пользователя
            String username = userDetails.getUsername();
            User currentUser = userService.findByUsername(username);

            if (currentUser == null || currentUser.getRole() != UserRole.ADMIN) {
                return "redirect:/login";
            }

        } catch (Exception e) {
            log.error("Error processing newsletter list for user: {}",
                    userDetails.getUsername(), e);
            return "redirect:/login";
        }
        model.addAttribute("users", Collections.emptyList());


        return "admin/add-coins";
    }

    @GetMapping("/add-coins/search")
    public String searchUsersForCoins(
            @RequestParam(required = false) String username,
            Model model
    ) {
        // Если username пуст, показываем null
        if (username != null && !username.trim().isEmpty()) {
            User user = userService.findByUsername(username);
            if (user != null) {
                model.addAttribute("user", user);
            }
        }

        return "admin/add-coins";
    }

    @PostMapping("/add-coins")
    public String addCoinsToUser(
            @RequestParam Long userId,
            @RequestParam BigDecimal amount,
            RedirectAttributes redirectAttributes,
            @AuthenticationPrincipal UserDetails adminDetails
    ) {
        try {
            User updatedUser = adService.addCoins(
                    userId,
                    amount,
                    adminDetails.getUsername()
            );

            redirectAttributes.addFlashAttribute("successMessage",
                    "Монеты успешно добавлены пользователю " + updatedUser.getUsername());
            return "redirect:/admin/users/add-coins";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка при добавлении монет: " + e.getMessage());
            return "redirect:/admin/users/add-coins";
        }
    }
}