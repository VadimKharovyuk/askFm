package com.example.askfm.controller;

import com.example.askfm.dto.UpcomingBirthdayDTO;
import com.example.askfm.dto.UserSearchDTO;
import com.example.askfm.model.User;
import com.example.askfm.service.BirthdayService;
import com.example.askfm.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@RequiredArgsConstructor
@Controller
public class HomePage {
    private final UserService userService;
    private final BirthdayService birthdayService;

    @GetMapping
    public String home() {
        return "home";
    }

@GetMapping("/contact")
public String contact(Model model) {
        return "contact";
}

    @GetMapping("/users/search")
    public String searchUsers(@RequestParam String query,
                              Model model,
                              @AuthenticationPrincipal UserDetails currentUser) {
        List<UserSearchDTO> users = userService.searchUsers(
                query,
                currentUser != null ? currentUser.getUsername() : null
        );
        model.addAttribute("users", users);
        return "user/search-results";
    }


}
