//package com.example.askfm.controller;
//
//import com.example.askfm.dto.QuestionDTO;
//import com.example.askfm.model.Question;
//import com.example.askfm.model.User;
//import com.example.askfm.service.QuestionService;
//import com.example.askfm.service.UserService;
//import jakarta.validation.Valid;
//import lombok.RequiredArgsConstructor;
//import org.springframework.security.core.annotation.AuthenticationPrincipal;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.validation.BindingResult;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//@Controller
//@RequiredArgsConstructor
//public class QuestionController {
//    private final QuestionService questionService;
//    private final UserService userService;
//
//    @GetMapping("/questions/ask/{username}")
//    public String showAskQuestionForm(@PathVariable String username, Model model) {
//        model.addAttribute("recipientUsername", username);
//        model.addAttribute("questionDTO", new QuestionDTO());
//        return "ask-question";
//    }
//
//    @PostMapping("/questions/ask")
//    public String askQuestion(@Valid @ModelAttribute QuestionDTO questionDTO,
//                              BindingResult result,
//                              @AuthenticationPrincipal User currentUser) {
//        if (result.hasErrors()) {
//            return "ask-question";
//        }
//
//        questionService.askQuestion(questionDTO, currentUser);
//        return "redirect:/users/" + questionDTO.getRecipientUsername();
//    }
//
//    @PostMapping("/questions/{questionId}/answer")
//    public String answerQuestion(@PathVariable Long questionId,
//                                 @RequestParam String content,
//                                 @AuthenticationPrincipal User currentUser) {
//        questionService.answerQuestion(questionId, content, currentUser);
//        return "redirect:/questions/received";
//    }
//
//    @GetMapping("/questions/received")
//    public String showReceivedQuestions(Model model, @AuthenticationPrincipal User currentUser) {
//        List<Question> questions = questionService.getReceivedQuestions(currentUser);
//        model.addAttribute("questions", questions);
//        return "received-questions";
//    }
//}