package com.example.askfm.controller;

import com.example.askfm.dto.BlockedUserDTO;
import com.example.askfm.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/blocks")
@RequiredArgsConstructor
@Slf4j
public class UserBlockController {

    private final UserService userService;

    // Страница со списком заблокированных пользователей
    @GetMapping
    public String getBlockedUsersList(
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {

        Page<BlockedUserDTO> blockedUsers = userService.getBlockedUsers(userDetails.getUsername());
        long blockedCount = userService.getBlockedUsersCount(userDetails.getUsername());

        model.addAttribute("blockedUsers", blockedUsers);
        model.addAttribute("blockedCount", blockedCount);

        return "blocks/list";
    }

    @PostMapping("/block/{username}")
    public String blockUser(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String username,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {

        try {
            userService.blockUser(userDetails.getUsername(), username);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Пользователь " + username + " успешно заблокирован");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        // Возвращаемся на страницу профиля пользователя
        return "redirect:/users/" + username;
    }

    // Разблокировать пользователя
    @PostMapping("/unblock/{username}")
    public String unblockUser(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String username,
            RedirectAttributes redirectAttributes) {

        try {
            userService.unblockUser(userDetails.getUsername(), username);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Пользователь " + username + " разблокирован");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/blocks";
//        return "redirect:/users/" + username;
    }



}