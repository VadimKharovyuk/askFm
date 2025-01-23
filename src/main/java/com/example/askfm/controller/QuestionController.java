package com.example.askfm.controller;

import com.example.askfm.dto.AnswerRequestDto;
import com.example.askfm.dto.QuestionDTO;
import com.example.askfm.dto.QuestionRequestDto;
import com.example.askfm.dto.QuestionResponseDto;
import com.example.askfm.model.Question;
import com.example.askfm.model.User;
import com.example.askfm.service.AnswerService;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
@Controller
@RequestMapping("/questions")
@RequiredArgsConstructor
public class QuestionController {
    private final QuestionService questionService;
    private final AnswerService answerService;
    private final UserService userService;

    // Обработка отправки вопроса
    @PostMapping("/ask")
    public String askQuestion(@ModelAttribute QuestionRequestDto questionRequest,
                              @AuthenticationPrincipal UserDetails userDetails,
                              RedirectAttributes redirectAttributes) {
        if (userDetails == null) {
            return "redirect:/login";
        }

        try {
            User author = userService.findByUsername(userDetails.getUsername());
            questionService.askQuestion(questionRequest, author.getId());

            // Получаем username получателя для редиректа
            User recipient = userService.findById(questionRequest.getRecipientId());
            return "redirect:/users/" + recipient.getUsername();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/error";
        }
    }

    // Удаление вопроса
    @PostMapping("/delete/{id}")
    public String deleteQuestion(@PathVariable Long id,
                                 @AuthenticationPrincipal UserDetails userDetails,
                                 RedirectAttributes redirectAttributes) {
        if (userDetails == null) {
            return "redirect:/login";
        }

        try {
            QuestionResponseDto question = questionService.getQuestionById(id);
            if (question.getRecipient().getUsername().equals(userDetails.getUsername())) {
                questionService.deleteQuestion(id);
                redirectAttributes.addFlashAttribute("message", "Вопрос успешно удален");
            } else {
                redirectAttributes.addFlashAttribute("error", "Вы не можете удалить этот вопрос");
            }
            return "redirect:/users/" + userDetails.getUsername();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/error";
        }
    }

    // Получение вопроса по ID
    @GetMapping("/{id}")
    public String getQuestion(@PathVariable Long id, Model model) {
        QuestionResponseDto question = questionService.getQuestionById(id);
        model.addAttribute("question", question);
        return "question-details";
    }


    @GetMapping("/ask/{username}")
    public String getAskQuestionPage(@PathVariable String username,
                                     @AuthenticationPrincipal UserDetails userDetails,
                                     Model model) {
        if (userDetails == null) {
            return "redirect:/login";
        }

        try {
            User recipient = userService.findByUsername(username);

            if (username.equals(userDetails.getUsername())) {
                model.addAttribute("error", "You cannot ask questions to yourself");
                return "error";
            }

            // Создаем DTO с уже установленным recipientId
            QuestionRequestDto questionRequest = new QuestionRequestDto();
            questionRequest.setRecipientId(recipient.getId());


            model.addAttribute("recipient", recipient);
            model.addAttribute("questionRequest", questionRequest);
            return "ask-question";
        } catch (Exception e) {
            model.addAttribute("error", "User not found: " + username);
            return "error";
        }
    }



    // Страница со списком полученных вопросов
    @GetMapping("/received")
    public String getReceivedQuestions(@AuthenticationPrincipal UserDetails userDetails,
                                       Model model) {
        if (userDetails == null) {
            return "redirect:/login";
        }

        try {
            User user = userService.findByUsername(userDetails.getUsername());
            var questions = questionService.getQuestionsForUser(user.getId());
            model.addAttribute("questions", questions);
            return "received-questions";
        } catch (Exception e) {
            model.addAttribute("error", "Error loading questions");
            return "error";
        }
    }

    // Страница для ответа на вопрос
    @GetMapping("/answer/{questionId}")
    public String getAnswerPage(@PathVariable Long questionId,
                                @AuthenticationPrincipal UserDetails userDetails,
                                Model model) {
        if (userDetails == null) {
            return "redirect:/login";
        }

        try {
            var question = questionService.getQuestionById(questionId);

            // Проверяем, что отвечает тот, кому задан вопрос
            User user = userService.findByUsername(userDetails.getUsername());
            if (!question.getRecipient().getId().equals(user.getId())) {
                model.addAttribute("error", "You can only answer questions asked to you");
                return "error";
            }

            model.addAttribute("question", question);
            model.addAttribute("answerRequest", new AnswerRequestDto());
            return "answer-question";
        } catch (Exception e) {
            model.addAttribute("error", "Question not found");
            return "error";
        }
    }

    // Обработка отправки ответа
    @PostMapping("/answer")
    public String answerQuestion(@ModelAttribute AnswerRequestDto answerRequest,
                                 @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return "redirect:/login";
        }

        try {
            User user = userService.findByUsername(userDetails.getUsername());
            answerService.createAnswer(answerRequest, user.getId());
            return "redirect:/users/" + userDetails.getUsername() + "/questions/answered";
        } catch (Exception e) {
            return "redirect:/error";
        }
    }

}



