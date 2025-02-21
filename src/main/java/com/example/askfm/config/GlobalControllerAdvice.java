package com.example.askfm.config;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalControllerAdvice {

    @ModelAttribute("currentUser")
    public String currentUser(@AuthenticationPrincipal UserDetails userDetails) {
        return userDetails != null ? userDetails.getUsername() : null;
    }
}