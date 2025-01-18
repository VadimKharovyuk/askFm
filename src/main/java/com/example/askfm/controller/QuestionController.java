package com.example.askfm.controller;

import com.example.askfm.dto.QuestionDTO;
import com.example.askfm.model.Question;
import com.example.askfm.model.User;
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

import java.util.List;
@Controller
@RequiredArgsConstructor
public class QuestionController {
    private final QuestionService questionService;

    @GetMapping("/questions/ask/{username}")
    public String showAskQuestionForm(@PathVariable String username, Model model) {
        model.addAttribute("recipientUsername", username);
        model.addAttribute("questionDTO", new QuestionDTO());
        return "question/ask";
    }

    @PostMapping("/questions/ask")
    public String askQuestion(@Valid @ModelAttribute QuestionDTO questionDTO,
                              @AuthenticationPrincipal UserDetails currentUser) {
        questionService.askQuestion(questionDTO, (User) currentUser);
        return "redirect:/users/" + questionDTO.getRecipientUsername();
    }

//    @GetMapping("/questions/received")
//    public String showReceivedQuestions(Model model, @AuthenticationPrincipal UserDetails currentUser) {
//        List<Question> questions = questionService.getReceivedQuestions(currentUser);
//        model.addAttribute("questions", questions);
//        return "question/received";
//    }
    @PostMapping("/questions/{questionId}/answer")
    public String answerQuestion(@PathVariable Long questionId,
                                 @RequestParam String content,
                                 @AuthenticationPrincipal UserDetails currentUser) {
        questionService.answerQuestion(questionId, content, (User) currentUser);
        return "redirect:/questions/received";
    }
}