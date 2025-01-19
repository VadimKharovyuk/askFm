package com.example.askfm.controller;

import com.example.askfm.model.User;
import com.example.askfm.service.QuestionService;
import com.example.askfm.service.SubscriptionService;
import com.example.askfm.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
@RequiredArgsConstructor
public class UserProfileController {
    private final UserService userService;
    private final QuestionService questionService;
    private final SubscriptionService subscriptionService;

    @GetMapping("/users/{username}")
    public String showUserProfile(@PathVariable String username,
                                  @AuthenticationPrincipal UserDetails currentUser,
                                  Model model) {
        User user = userService.findByUsername(username);

        boolean isFollowing = false;
        if (currentUser != null) {
            isFollowing = subscriptionService.isFollowing(currentUser.getUsername(), username);
        }

        model.addAttribute("profileUser", user);
        model.addAttribute("isFollowing", isFollowing);
        model.addAttribute("followersCount", subscriptionService.getSubscribersCount(username));
        model.addAttribute("followingCount", subscriptionService.getSubscriptionsCount(username));
        model.addAttribute("questions", questionService.getAnsweredQuestions(user));
        model.addAttribute("currentUser", currentUser != null ? currentUser.getUsername() : null);

        return "user/profile-view";
    }
}
