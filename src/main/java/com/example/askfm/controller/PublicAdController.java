package com.example.askfm.controller;

import com.example.askfm.dto.AdLeadFormDTO;
import com.example.askfm.dto.AdPublicDto;
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

@Controller
@RequestMapping("/public/ads")
@RequiredArgsConstructor
public class PublicAdController {
    private final AdService adService;
    private final AdLeadService adLeadService;
    private final UserService userService;

    // Показ рекламы с формой
    @GetMapping("/{adId}")
    public String showPublicAd(@PathVariable Long adId,
                               Model model,
                               @AuthenticationPrincipal UserDetails userDetails) {
        // Получаем публичные данные рекламы
        AdPublicDto ad = adService.getPublicAd(adId);

        // Получаем данные пользователя для автозаполнения формы
        User user = userService.findByUsername(userDetails.getUsername());

        // Создаем DTO для формы и заполняем данными пользователя
        AdLeadFormDTO leadFormDTO = new AdLeadFormDTO();
        leadFormDTO.setUsername(user.getUsername());
        leadFormDTO.setEmail(user.getEmail());

        // Добавляем данные в модель
        model.addAttribute("ad", ad);
        model.addAttribute("leadForm", leadFormDTO);

        return "public/ad-view";
    }

    // Обработка отправки формы
    @PostMapping("/{adId}/submit")
    public String submitLead(@PathVariable Long adId,
                             @Valid @ModelAttribute("leadForm") AdLeadFormDTO leadFormDTO,
                             BindingResult bindingResult,
                             @AuthenticationPrincipal UserDetails userDetails,
                             Model model) {
        // Проверка валидации
        if (bindingResult.hasErrors()) {
            model.addAttribute("ad", adService.getPublicAd(adId));
            return "public/ad-view";
        }

        try {
            // Сохраняем лид и обновляем статистику
            adLeadService.submitLead(adId, userDetails.getUsername(), leadFormDTO);
            return "ad/lead-success";
        } catch (Exception e) {
            model.addAttribute("error", "Произошла ошибка при отправке формы");
            model.addAttribute("ad", adService.getPublicAd(adId));
            return "public/ad-view";
        }
    }

    // Страница успешной отправки формы
    @GetMapping("/success")
    public String showSuccess(Model model ,@AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByUsername(userDetails.getUsername());
        model.addAttribute("user", user);
        return "ad/lead-success";
    }
}