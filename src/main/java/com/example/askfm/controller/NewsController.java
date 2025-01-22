package com.example.askfm.controller;

import com.example.askfm.dto.NewsDTO;
import com.example.askfm.enums.NewsCategory;
import com.example.askfm.service.NewsService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/news")
@RequiredArgsConstructor
public class NewsController {
    private final NewsService newsService;

    @GetMapping
    public String showAllNews(Model model,
                              @RequestParam(required = false) NewsCategory category,
                              @AuthenticationPrincipal UserDetails userDetails) {
        List<NewsDTO> newsList;
        if (category != null) {
            newsList = newsService.getNewsByCategory(category).stream()
                    .map(newsService::convertToDTO)
                    .collect(Collectors.toList());
        } else {
            newsList = newsService.getAllPublishedNews().stream()
                    .map(newsService::convertToDTO)
                    .collect(Collectors.toList());
        }

        model.addAttribute("newsList", newsList);
        model.addAttribute("categories", NewsCategory.values());
        model.addAttribute("selectedCategory", category);
        model.addAttribute("currentUser", userDetails != null ? userDetails.getUsername() : null);

        return "news/index";
    }

    @GetMapping("/{id}")
    @Transactional
    public String showNewsDetails(@PathVariable Long id, Model model,
                                  @AuthenticationPrincipal UserDetails userDetails) {
        NewsDTO news = newsService.convertToDTO(newsService.getNewsById(id));
        newsService.incrementViewCount(id);

        model.addAttribute("news", news);
        model.addAttribute("currentUser", userDetails != null ? userDetails.getUsername() : null);

        return "news/details";
    }
}
