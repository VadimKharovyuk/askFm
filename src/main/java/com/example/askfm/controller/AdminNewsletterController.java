package com.example.askfm.controller;

import com.example.askfm.dto.NewsletterDTO;
import com.example.askfm.enums.UserRole;
import com.example.askfm.model.User;
import com.example.askfm.service.NewsletterService;
import com.example.askfm.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
@Slf4j
@Controller
@RequestMapping("/admin/newsletters")

@RequiredArgsConstructor
public class AdminNewsletterController {
    private final NewsletterService newsletterService;
    private final UserService userService;

    @GetMapping
    public String showNewsletterList(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        // Получаем пользователя и проверяем его роль
        User currentUser = userService.findByUsername(userDetails.getUsername());
        if (currentUser.getRole() != UserRole.ADMIN) {
            return "redirect:/login";
        }
        model.addAttribute("newsletters", newsletterService.getAllNewsletters());
        return "admin/newsletters/list";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("newsletterDTO", new NewsletterDTO());
        return "admin/newsletters/create";
    }
    @PostMapping("/create")
    public String createNewsletter(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @ModelAttribute NewsletterDTO dto,
            BindingResult result,
            RedirectAttributes redirectAttributes) {

        // Получаем пользователя
        User currentUser = userService.findByUsername(userDetails.getUsername());

        log.info("Получен запрос на создание рассылки: {}", dto);
        log.info("Текущий пользователь: {}", currentUser.getUsername());

        if (result.hasErrors()) {
            return "admin/newsletters/create";
        }

        try {
            newsletterService.createNewsletter(dto, currentUser.getId());
            redirectAttributes.addFlashAttribute("success",
                    "Рассылка успешно " + (dto.isDraft() ? "сохранена" : "запланирована"));
        } catch (Exception e) {
            log.error("Ошибка при создании рассылки: ", e);
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/newsletters";
    }

    @PostMapping("/{id}/cancel")
    public String cancelNewsletter(@PathVariable Long id,
                                   RedirectAttributes redirectAttributes) {
        try {
            newsletterService.cancelNewsletter(id);
            redirectAttributes.addFlashAttribute("success", "Рассылка отменена");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/newsletters";
    }
}
