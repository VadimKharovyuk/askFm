package com.example.askfm.service;

import com.example.askfm.enums.UserRole;
import com.example.askfm.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthorizationService {
    private final UserService userService;

    public boolean isAdmin(UserDetails userDetails) {
        if (userDetails == null) {
            return false;
        }

        try {
            User currentUser = userService.findByUsername(userDetails.getUsername());
            return currentUser != null && currentUser.getRole() == UserRole.ADMIN;
        } catch (Exception e) {
            log.error("Error checking admin status for user: {}",
                    userDetails.getUsername(), e);
            return false;
        }
    }

    public String redirectIfNotAdmin(UserDetails userDetails) {
        return isAdmin(userDetails) ? null : "redirect:/login";
    }
}