package com.example.askfm.controller;

import com.example.askfm.dto.PostDTO;
import com.example.askfm.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/trending")
@RequiredArgsConstructor
public class SearchController {
    private final SearchService searchService;



    @GetMapping("/most-liked")
    public String getMostLikedPosts(@RequestParam(defaultValue = "0") int page,
                                    @RequestParam(defaultValue = "10") int size,
                                    Model model,
                                    @AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails != null ? userDetails.getUsername() : null;
        List<PostDTO> mostLikedPosts = searchService.getMostLikedPosts(username);
        model.addAttribute("posts", mostLikedPosts);
        model.addAttribute("title", "Most Liked Posts");
        return "search/results";
    }

    @GetMapping("/most-viewed")
    public String getMostViewedPosts(@RequestParam(defaultValue = "0") int page,
                                     @RequestParam(defaultValue = "10") int size,
                                     Model model,
                                     @AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails != null ? userDetails.getUsername() : null;
        List<PostDTO> mostViewedPosts = searchService.getMostViewedPosts(username);
        model.addAttribute("posts", mostViewedPosts);
        model.addAttribute("title", "Most Viewed Posts");
        return "search/results";
    }

    @GetMapping("/recent")
    public String getRecentPosts(@RequestParam(defaultValue = "0") int page,
                                 @RequestParam(defaultValue = "10") int size,
                                 Model model,
                                 @AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails != null ? userDetails.getUsername() : null;
        List<PostDTO> recentPosts = searchService.getRecentPosts(username);
        model.addAttribute("posts", recentPosts);
        model.addAttribute("title", "Recent Posts");
        return "search/results";
    }
}