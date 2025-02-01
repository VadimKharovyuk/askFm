package com.example.askfm.controller;

import com.example.askfm.dto.AdCreateDto;
import com.example.askfm.dto.AdLeadFormDTO;
import com.example.askfm.dto.AdLeadResponseDTO;
import com.example.askfm.dto.AdResponseDto;
import com.example.askfm.model.Ad;
import com.example.askfm.model.User;
import com.example.askfm.service.AdLeadService;
import com.example.askfm.service.AdService;
import com.example.askfm.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@Slf4j
@RequiredArgsConstructor
@Controller
@RequestMapping("/ads")
public class AdController {

    private final AdService adService;
    private final UserService userService;
    private final AdLeadService adLeadService;

    @GetMapping
    public String adsHome(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.findByUsername(userDetails.getUsername());

        // Получаем список активных реклам текущего пользователя
        List<AdResponseDto> activeAds = adService.getActiveAdsByUser(currentUser.getId());

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("activeAds", activeAds);
        return "ad/dashboard";
    }

    @GetMapping("/{adId}/leads")
    public String showAdLeads(
            @PathVariable Long adId,
            @RequestParam(defaultValue = "0") int page,
            Model model) {

        Page<AdLeadResponseDTO> leadsPage = adLeadService.getAdLeadsByAdId(adId, page);
        Ad ad = adService.findById(adId);

        model.addAttribute("ad", ad);
        model.addAttribute("leads", leadsPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", leadsPage.getTotalPages());

        return "ad/ad-leads";
    }
    @PostMapping("/{id}/delete")
    public String deleteAd(@PathVariable Long id) {
        adService.deleteAd(id);
        return "redirect:/ads";
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
            return "redirect:/ads/lead-success";
        } catch (Exception e) {
            model.addAttribute("error", "Произошла ошибка при отправке формы");
            model.addAttribute("ad", adService.getPublicAd(adId));
            return "test/ad-view";
        }
    }

//лид записался
    @GetMapping("/lead-success")
    public String showLeadSuccess(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        model.addAttribute("user", userService.findByUsername(userDetails.getUsername()));
        return "ad/lead-success";
    }


    // Просмотр активных реклам
    @GetMapping("/active")
    public String getActiveAds(Model model) {
        model.addAttribute("activeAds", adService.getActiveAds());
        return "ad/active"; // Страница для отображения активных реклам
    }


}