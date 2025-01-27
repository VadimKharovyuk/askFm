package com.example.askfm.controller;

import com.example.askfm.dto.UserSearchDTO;
import com.example.askfm.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserSearchRestController {
    private final UserService userService;

    @GetMapping("/users/search")
    public List<UserSearchDTO> searchUsers(@RequestParam String query,
                                           @AuthenticationPrincipal UserDetails currentUser) {
        return userService.searchUsers(
                query,
                currentUser != null ? currentUser.getUsername() : null
        );
    }
}
