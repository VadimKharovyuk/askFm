package com.example.askfm.controller;

import com.example.askfm.dto.SavedPostDTO;
import com.example.askfm.service.SavedPostService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/saved")
@RequiredArgsConstructor
public class SavedPostController {
    private final SavedPostService savedPostService;

    @GetMapping
    public String getSavedPosts(@AuthenticationPrincipal UserDetails userDetails,
                                Model model) {
        List<SavedPostDTO> savedPosts = savedPostService.getUserSavedPosts(userDetails.getUsername());
        model.addAttribute("savedPosts", savedPosts);
        model.addAttribute("currentUser", userDetails.getUsername());
        return "saved/saved-posts";
    }


    @PostMapping("/remove/{postId}")
    public String removeSavedPost(@PathVariable Long postId,
                                  @AuthenticationPrincipal UserDetails userDetails) {
        savedPostService.removeSavedPost(postId, userDetails.getUsername());
        return "redirect:/saved";
    }


    @PostMapping("/save/{postId}")
    public String savePost(@PathVariable Long postId,
                           @AuthenticationPrincipal UserDetails userDetails,
                           RedirectAttributes redirectAttributes) {
        savedPostService.savePost(postId, userDetails.getUsername());
        return "redirect:/posts/" + postId;
    }


}
