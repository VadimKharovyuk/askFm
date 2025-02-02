package com.example.askfm.controller;


import com.example.askfm.service.PasswordService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/forgot-password")
public class PasswordResetController {

    private final PasswordService passwordService;

    @GetMapping
    public String showForgotPasswordForm() {
        return "aut/forgot-password";
    }

    @PostMapping("/reset-password")
    public String processForgotPassword(@RequestParam("email") String email,
                                        RedirectAttributes redirectAttributes) {
        try {
            passwordService.resetPassword(email);
            redirectAttributes.addFlashAttribute("success",
                    "Инструкции по восстановлению пароля отправлены на вашу почту");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/forgot-password";
    }
}