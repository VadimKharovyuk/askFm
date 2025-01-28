package com.example.askfm.controller;

import com.example.askfm.dto.ChangePasswordDTO;
import com.example.askfm.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
        String username = userDetails.getUsername();
        model.addAttribute("username", username);
        return "settings/home";
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
