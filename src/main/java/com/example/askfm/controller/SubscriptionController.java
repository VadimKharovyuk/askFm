package com.example.askfm.controller;

import com.example.askfm.dto.UserSearchDTO;
import com.example.askfm.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class SubscriptionController {
    private final SubscriptionService subscriptionService;

    @PostMapping("/users/{username}/follow")
    public String followUser(@PathVariable String username,
                             @AuthenticationPrincipal UserDetails currentUser) {
        subscriptionService.follow(currentUser.getUsername(), username);
        return "redirect:/users/" + username;
    }

    @PostMapping("/users/{username}/unfollow")
    public String unfollowUser(@PathVariable String username,
                               @AuthenticationPrincipal UserDetails currentUser) {
        subscriptionService.unfollow(currentUser.getUsername(), username);
        return "redirect:/users/" + username;
    }


    @GetMapping("/users/{username}/following")
    public String showFollowing(@PathVariable String username,
                                Model model,
                                @AuthenticationPrincipal UserDetails currentUser) {
        List<UserSearchDTO> following = subscriptionService.getFollowingUsers(
                username,
                currentUser != null ? currentUser.getUsername() : null
        );
        model.addAttribute("users", following);
        model.addAttribute("username", username);
        model.addAttribute("listType", "Following");
        return "user/subscription-list";
    }

    @GetMapping("/users/{username}/followers")
    public String showFollowers(@PathVariable String username,
                                Model model,
                                @AuthenticationPrincipal UserDetails currentUser) {
        List<UserSearchDTO> followers = subscriptionService.getFollowers(
                username,
                currentUser != null ? currentUser.getUsername() : null
        );
        model.addAttribute("users", followers);
        model.addAttribute("username", username);
        model.addAttribute("listType", "Followers");
        return "user/subscription-list";
    }
}