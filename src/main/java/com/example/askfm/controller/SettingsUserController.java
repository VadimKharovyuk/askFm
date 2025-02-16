package com.example.askfm.controller;

import com.example.askfm.dto.ChangePasswordDTO;
import com.example.askfm.dto.UsernameUpdateDto;
import com.example.askfm.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/settings")
public class SettingsUserController {
    private final UserService userService;

    @GetMapping
    public String settings(Model model,
                           @AuthenticationPrincipal UserDetails userDetails) {
        String currentUser = userDetails.getUsername();
        Long blockedCount = userService.getBlockedUsersCount(currentUser);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("blockedCount", blockedCount);
        return "settings/home";
    }


    @GetMapping("/updateUsername")
    public String updateUsername(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        model.addAttribute("username", userDetails.getUsername());
        model.addAttribute("usernameUpdateDto", new UsernameUpdateDto());
        return "settings/updateUsername";
    }

    @PostMapping("/updateUsername")
    public String updateName(@Valid @ModelAttribute("usernameUpdateDto") UsernameUpdateDto usernameUpdateDto,
                             BindingResult bindingResult,
                             @AuthenticationPrincipal UserDetails userDetails,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("username", userDetails.getUsername());
            return "settings/updateUsername";
        }

        try {
            String oldUsername = userDetails.getUsername();
            userService.updateUsername(oldUsername, usernameUpdateDto.getNewUsername());

            redirectAttributes.addFlashAttribute("successMessage",
                    "Имя пользователя успешно изменено на " + usernameUpdateDto.getNewUsername());

            return "redirect:/home";
        } catch (IllegalArgumentException e) {
            model.addAttribute("username", userDetails.getUsername());
            model.addAttribute("error", "Это имя пользователя уже занято");
            return "settings/updateUsername";
        }
    }



    @GetMapping("/password")
    public String passwordSettings(Model model,
                                   @AuthenticationPrincipal UserDetails userDetails) {
        model.addAttribute("username", userDetails.getUsername());
        model.addAttribute("passwordForm", new ChangePasswordDTO());
        return "settings/password";
    }

    @PostMapping("/change-password")
    public String changePassword(@Valid @ModelAttribute("passwordForm") ChangePasswordDTO passwordForm,
                                 BindingResult bindingResult,
                                 @AuthenticationPrincipal UserDetails userDetails,
                                 RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    bindingResult.getFieldError().getDefaultMessage());
            return "redirect:/settings/password";
        }

        try {
            userService.changePassword(userDetails.getUsername(), passwordForm);
            redirectAttributes.addFlashAttribute("successMessage", "Пароль успешно изменен");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/settings/password";
    }

    @GetMapping("/avatar")
    public String avatarSettings(Model model,
                                 @AuthenticationPrincipal UserDetails userDetails) {
        model.addAttribute("username", userDetails.getUsername());
        return "settings/avatar";
    }


    @GetMapping("/privacy")
    public String privacySettings(Model model,
                                  @AuthenticationPrincipal UserDetails userDetails) {
        model.addAttribute("username", userDetails.getUsername());
        return "settings/privacy";
    }

    @GetMapping("/profile")
    public String profileSettings(Model model,
                                  @AuthenticationPrincipal UserDetails userDetails) {
        model.addAttribute("username", userDetails.getUsername());
        return "settings/profile";
    }

    @GetMapping("/notifications")
    public String notificationSettings(Model model,
                                       @AuthenticationPrincipal UserDetails userDetails) {
        model.addAttribute("username", userDetails.getUsername());
        return "settings/notifications";
    }

    @GetMapping("/security")
    public String securitySettings(Model model,
                                   @AuthenticationPrincipal UserDetails userDetails) {
        model.addAttribute("username", userDetails.getUsername());
        return "settings/security";
    }
}
