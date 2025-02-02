package com.example.askfm.controller;

import com.example.askfm.service.NewsletterService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/newsletter")
@RequiredArgsConstructor
public class NewsletterSubscriptionController {
    private final NewsletterService newsletterService;

    @PostMapping("/subscribe")
    public String subscribe(@RequestParam String email,
                            RedirectAttributes redirectAttributes) {
        try {
            newsletterService.subscribeToNewsletter(email);
            redirectAttributes.addFlashAttribute("success",
                    "Вы успешно подписались на рассылку!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/";
    }
}