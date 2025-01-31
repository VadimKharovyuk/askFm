package com.example.askfm.controller;

import com.example.askfm.dto.AdLeadFormDTO;
import com.example.askfm.dto.AdPublicDto;
import com.example.askfm.model.Ad;
import com.example.askfm.model.User;
import com.example.askfm.service.AdLeadService;
import com.example.askfm.service.AdService;
import com.example.askfm.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/test/ads")
@RequiredArgsConstructor
public class TestAdController {
    private final AdService adService;
    private final AdLeadService adLeadService;
    private final UserService userService;

    // Показ случайной рекламы
    @GetMapping("/random")
    public String showRandomAd(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            // Получаем случайную рекламу
            AdPublicDto ad = adService.getRandomAd();

            // Получаем данные пользователя
            User user = userService.findByUsername(userDetails.getUsername());

            // Создаем DTO для формы
            AdLeadFormDTO leadFormDTO = new AdLeadFormDTO();
            leadFormDTO.setUsername(user.getUsername());
            leadFormDTO.setEmail(user.getEmail());

            model.addAttribute("ad", ad);
            model.addAttribute("leadForm", leadFormDTO);

            return "test/ad-view";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }

    // Обработка отправки формы
    @PostMapping("/submit")
    public String submitLead(@RequestParam Long adId,
                             @Valid @ModelAttribute("leadForm") AdLeadFormDTO leadFormDTO,
                             BindingResult bindingResult,
                             @AuthenticationPrincipal UserDetails userDetails,
                             Model model) {
        // Проверка валидации
        if (bindingResult.hasErrors()) {
            model.addAttribute("ad", adService.getPublicAd(adId));
            return "test/ad-view";
        }

        try {
            // Сохраняем лид и обновляем статистику
            adLeadService.submitLead(adId, userDetails.getUsername(), leadFormDTO);
            return "redirect:/ad/lead-success";
        } catch (Exception e) {
            model.addAttribute("error", "Произошла ошибка при отправке формы");
            model.addAttribute("ad", adService.getPublicAd(adId));
            return "test/ad-view";
        }
    }

    // Страница успешной отправки
    @GetMapping("/success")
    public String showSuccess() {
        return "ad/lead-success";
    }
}