package com.example.askfm.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

// Только для разработки! Удалить перед production
@RestController
@RequestMapping("/dev")
public class DevToolsController {

    @Autowired
    private PasswordEncoder passwordEncoder;


    @GetMapping("/hash")
    public String generateHash(@RequestParam String password) {
        return passwordEncoder.encode(password);
    }
}