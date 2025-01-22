package com.example.askfm.controller;

import com.example.askfm.dto.NewsDTO;
import com.example.askfm.enums.NewsCategory;
import com.example.askfm.enums.UserRole;
import com.example.askfm.model.User;
import com.example.askfm.service.NewsService;
import com.example.askfm.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/news")
@RequiredArgsConstructor
public class AdminNewsController {
    private final NewsService newsService;
    private final UserService userService;

    @GetMapping
    public String showNews(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.findByUsername(userDetails.getUsername());
        if (currentUser.getRole() == UserRole.ADMIN) {
            return "redirect:/login";
        }

        model.addAttribute("newsList", newsService.getRecentNews().stream()
                .map(newsService::convertToDTO)
                .collect(Collectors.toList()));
        model.addAttribute("categories", NewsCategory.values());
        model.addAttribute("newNewsDTO", new NewsDTO());
        model.addAttribute("adminUsername", currentUser.getUsername());

        return "admin/news";
    }

    @PostMapping("/create")
    public String createNews(@ModelAttribute NewsDTO newsDTO,
                             @AuthenticationPrincipal UserDetails userDetails) {
        newsService.createNews(newsDTO, userDetails.getUsername());
        return "redirect:/admin/news";
    }

    @PostMapping("/{id}/update")
    public String updateNews(@PathVariable Long id, @ModelAttribute NewsDTO newsDTO) {
        newsService.updateNews(id, newsDTO);
        return "redirect:/admin/news";
    }
}
