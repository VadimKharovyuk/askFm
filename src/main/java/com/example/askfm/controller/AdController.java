package com.example.askfm.controller;

import com.example.askfm.dto.AdCreateDto;
import com.example.askfm.dto.AdLeadFormDTO;
import com.example.askfm.dto.AdResponseDto;
import com.example.askfm.model.Ad;
import com.example.askfm.model.User;
import com.example.askfm.service.AdLeadService;
import com.example.askfm.service.AdService;
import com.example.askfm.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@Controller
@RequestMapping("/ads")
public class AdController {

    private final AdService adService;
    private final UserService userService;



    @GetMapping
    public String adsHome(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.findByUsername(userDetails.getUsername());

        // Получаем список активных реклам текущего пользователя
        List<AdResponseDto> activeAds = adService.getActiveAdsByUser(currentUser.getId());

        model.addAttribute("user", currentUser);
        model.addAttribute("activeAds", activeAds);
        return "ad/dashboard";
    }

    // Отображение формы для создания рекламы
    @GetMapping("/create")
    public String showAdCreationForm(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails.getUsername();
        User user = userService.findByUsername(username);
        model.addAttribute("adCreateDto", new AdCreateDto());
        model.addAttribute("username", username);
        model.addAttribute("balance", user.getBalance());
        model.addAttribute("userId", user.getId());
        return "ad/create";
    }

    // Обработка данных формы для создания рекламы
    @PostMapping("/create")
    public String createAd(@RequestParam Long userId, @ModelAttribute AdCreateDto adCreateDto, Model model) {
        try {
            AdResponseDto adResponseDto = adService.createAd(userId, adCreateDto);
            model.addAttribute("ad", adResponseDto);
            return "ad/success"; // Страница успеха (после успешного создания)
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "ad/create"; // В случае ошибки возвращаем на форму с ошибкой
        }
    }

    // Просмотр активных реклам
    @GetMapping("/active")
    public String getActiveAds(Model model) {
        model.addAttribute("activeAds", adService.getActiveAds());
        return "ad/active"; // Страница для отображения активных реклам
    }


}