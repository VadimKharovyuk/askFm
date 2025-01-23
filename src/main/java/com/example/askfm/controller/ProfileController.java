package com.example.askfm.controller;

import com.example.askfm.model.User;
import com.example.askfm.service.ImageService;
import com.example.askfm.service.QuestionService;
import com.example.askfm.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;
import java.util.Objects;

@Controller
@RequiredArgsConstructor
public class ProfileController {
    private final UserService userService;
    private final ImageService imageService;


    @GetMapping("/home")
    public String home(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByUsername(userDetails.getUsername());

        model.addAttribute("username", userDetails.getUsername());
        model.addAttribute("profileUser", user);
        model.addAttribute("currentUser", userDetails.getUsername());
        model.addAttribute("coverBase64", imageService.getBase64Avatar(user.getCover()));
        model.addAttribute("avatarBase64", imageService.getBase64Avatar(user.getAvatar()));
        return "user/profile";
    }

    @PostMapping("/profile/avatar")
    public String updateAvatar(@RequestParam("avatar") MultipartFile file,
                               @AuthenticationPrincipal UserDetails currentUser) {
        try {
            if (!Objects.requireNonNull(file.getContentType()).startsWith("image/")) {
                throw new IllegalArgumentException("Разрешены только изображения");
            }

            if (file.getSize() > 5 * 1024 * 1024) {
                throw new IllegalArgumentException("Размер файла должен быть меньше 5MB");
            }

            byte[] resizedImage = imageService.resizeImage(file.getBytes(), 300);
            userService.updateAvatar(currentUser.getUsername(), resizedImage);

            return "redirect:/home";
        } catch (IOException e) {
            return "redirect:/home?error=true";
        }


    }

    @PostMapping("/profile/cover")
    public String updateCover(@RequestParam("cover") MultipartFile file,
                              @AuthenticationPrincipal UserDetails currentUser) {
        try {
            if (!Objects.requireNonNull(file.getContentType()).startsWith("image/")) {
                throw new IllegalArgumentException("Разрешены только изображения");
            }

            if (file.getSize() > 5 * 1024 * 1024) {
                throw new IllegalArgumentException("Размер файла должен быть меньше 5MB");
            }

            byte[] resizedImage = imageService.resizeImage(file.getBytes(), 600);
            userService.updateCover(currentUser.getUsername(), resizedImage);

            return "redirect:/home";
        } catch (IOException e) {
            return "redirect:/home?error=true";
        }

    }
}