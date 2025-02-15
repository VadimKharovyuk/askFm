package com.example.askfm.controller;

import com.example.askfm.dto.PostDTO;
import com.example.askfm.service.TopLikedPostsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/trending/all")
@RequiredArgsConstructor
public class AllTrendingPostsController {

    private final TopLikedPostsService topLikedPostsService;

    @GetMapping
    public String showAllTrendingPosts(@AuthenticationPrincipal UserDetails userDetails,
                                       @PageableDefault(size = 20) Pageable pageable,
                                       Model model) {
        String currentUsername = userDetails != null ? userDetails.getUsername() : null;
        Page<PostDTO> topPosts = topLikedPostsService.getAllTopLikedPosts(pageable, currentUsername);

        model.addAttribute("posts", topPosts);
        model.addAttribute("currentUser", currentUsername);

        return "trending/all-trending-posts";
    }
}