package com.example.askfm.controller;

import com.example.askfm.dto.QuestionResponseDto;
import com.example.askfm.model.User;
import com.example.askfm.service.QuestionService;
import com.example.askfm.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/users/{username}/questions")
@RequiredArgsConstructor
public class UserQuestionsController {
    private final QuestionService questionService;
    private final UserService userService;

    // Страница с вопросами, на которые пользователь ответил
    @GetMapping("/answered")
    public String getAnsweredQuestions(@PathVariable String username,
                                       @AuthenticationPrincipal UserDetails currentUser,
                                       Model model) {
        User profileUser = userService.findByUsername(username);
        List<QuestionResponseDto> answeredQuestions = questionService.getAnsweredQuestions(profileUser.getId());

        model.addAttribute("questions", answeredQuestions);
        model.addAttribute("profileUser", profileUser);
        model.addAttribute("currentUser", currentUser != null ? currentUser.getUsername() : null);
        model.addAttribute("activeTab", "answered");

        return "user/questions-answered";
    }

    // Страница с неотвеченными вопросами (только для владельца профиля)
    @GetMapping("/received")
    public String getReceivedQuestions(@PathVariable String username,
                                       @AuthenticationPrincipal UserDetails currentUser,
                                       Model model) {
        if (currentUser == null || !username.equals(currentUser.getUsername())) {
            return "redirect:/login";
        }

        User profileUser = userService.findByUsername(username);
        List<QuestionResponseDto> unansweredQuestions = questionService.getUnansweredQuestions(profileUser.getId());

        model.addAttribute("questions", unansweredQuestions);
        model.addAttribute("profileUser", profileUser);
        model.addAttribute("currentUser", currentUser.getUsername());
        model.addAttribute("activeTab", "received");

        return "user/questions-received";
    }
}
