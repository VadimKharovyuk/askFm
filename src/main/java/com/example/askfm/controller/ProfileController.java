//package com.example.askfm.controller;
//
//import com.example.askfm.dto.UserProfileDTO;
//import com.example.askfm.model.User;
//import com.example.askfm.service.QuestionService;
//import com.example.askfm.service.UserService;
//import jakarta.validation.Valid;
//import lombok.RequiredArgsConstructor;
//import org.springframework.security.core.annotation.AuthenticationPrincipal;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.validation.BindingResult;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.ModelAttribute;
//import org.springframework.web.bind.annotation.PathVariable;
//import org.springframework.web.bind.annotation.PostMapping;
//
//@Controller
//@RequiredArgsConstructor
//public class ProfileController {
//    private final UserService userService;
//    private final QuestionService questionService;
//
//    @GetMapping("/users/{username}")
//    public String showProfile(@PathVariable String username, Model model) {
//        User user = userService.findByUsername(username)
//                .orElseThrow(() -> new IllegalArgumentException("User not found"));
//
//        model.addAttribute("user", user);
//        model.addAttribute("answeredQuestions",
//                questionService.getAnsweredQuestions(user));
//        return "profile";
//    }
//
//    @GetMapping("/profile/edit")
//    public String showEditProfileForm(Model model, @AuthenticationPrincipal User currentUser) {
//        UserProfileDTO profileDTO = new UserProfileDTO();
//        profileDTO.setBio(currentUser.getBio());
//        model.addAttribute("profileDTO", profileDTO);
//        return "edit-profile";
//    }
//
//    @PostMapping("/profile/edit")
//    public String updateProfile(@Valid @ModelAttribute UserProfileDTO profileDTO,
//                                BindingResult result,
//                                @AuthenticationPrincipal User currentUser) {
//        if (result.hasErrors()) {
//            return "edit-profile";
//        }
//
//        userService.updateProfile(currentUser, profileDTO);
//        return "redirect:/users/" + currentUser.getUsername();
//    }
//}