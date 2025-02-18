package com.example.askfm.controller;

import com.example.askfm.dto.PhotoStatDTO;
import com.example.askfm.dto.PhotoUnlockStatDTO;
import com.example.askfm.service.PhotoStatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
@Slf4j
@Controller
@RequestMapping("/photo-stats")
@RequiredArgsConstructor
public class PhotoStatsController {
    private final PhotoStatisticsService photoStatisticsService;


    @GetMapping
    public String showPhotoStats(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "uploads") String type,
            @AuthenticationPrincipal UserDetails currentUser,
            Model model) {

        Pageable pageable = PageRequest.of(page, size);

        switch (type) {
            case "uploads" -> {
                log.debug("Запрос статистики по загрузкам, страница: {}", page);
                Page<PhotoStatDTO> uploadStats = photoStatisticsService.getTopPhotoUploaders(pageable);
                model.addAttribute("uploadStats", uploadStats);
            }
            case "unlocks" -> {
                log.debug("Запрос статистики по разблокировкам, страница: {}", page);
                Page<PhotoUnlockStatDTO> unlockStats = photoStatisticsService.getTopUnlockedPhotos(pageable);
                model.addAttribute("unlockStats", unlockStats);
            }
            default -> {
                log.warn("Неизвестный тип статистики: {}", type);
                return "redirect:/photo-stats?type=uploads";
            }
        }

        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        model.addAttribute("statType", type);

        return "photos/photo-stats";
    }


    // Вспомогательный метод для получения отдельной статистики
    @GetMapping("/top-uploaders")
    @ResponseBody
    public Page<PhotoStatDTO> getTopUploaders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return photoStatisticsService.getTopPhotoUploaders(PageRequest.of(page, size));
    }


    // Вспомогательный метод для получения отдельной статистики
    @GetMapping("/top-unlocked")
    @ResponseBody
    public Page<PhotoUnlockStatDTO> getTopUnlocked(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return photoStatisticsService.getTopUnlockedPhotos(PageRequest.of(page, size));
    }

}
