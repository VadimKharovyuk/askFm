package com.example.askfm.controller;

import com.example.askfm.dto.UserSuggestionDTO;
import com.example.askfm.model.User;
import com.example.askfm.service.SuggestedUsersService;
import com.example.askfm.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class UserSuggestionsController {
    private final SuggestedUsersService suggestedUsersService;
    private final UserService userService;
    private static final int PAGE_SIZE = 20;

    @GetMapping("/users/suggestions")
    public String showAllSuggestions(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            Model model
    ) {
        if (userDetails == null) {
            return "redirect:/login";
        }


        String username = userDetails.getUsername();
        User currentUser = userService.findByUsername(username);
        model.addAttribute("currentUser", currentUser);


        Page<UserSuggestionDTO> users = suggestedUsersService
                .getPaginatedSuggestedUsers(currentUser.getUsername(), PageRequest.of(page, PAGE_SIZE));


        model.addAttribute("users", users.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", users.getTotalPages());
        model.addAttribute("hasNext", users.hasNext());
        model.addAttribute("hasPrevious", users.hasPrevious());

        return "user/suggestions";
    }
}
