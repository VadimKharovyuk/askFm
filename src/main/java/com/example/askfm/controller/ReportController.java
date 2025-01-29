package com.example.askfm.controller;

import com.example.askfm.dto.PostReportCreateDTO;
import com.example.askfm.maper.PostReportMapper;
import com.example.askfm.model.Post;
import com.example.askfm.service.PostReportService;
import com.example.askfm.service.PostService;
import com.example.askfm.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/report")
public class ReportController {
    private final PostReportService reportService;
    private final PostService postService;
    private final UserService userService;


    @PostMapping("/lockUser")
    public String lockUser(@RequestParam String username,
                           @RequestParam(required = false, defaultValue = "Нарушение правил сообщества") String reason) {
        log.info("Attempting to lock user: {}", username);
        try {
            userService.lockUser(username, reason);
            log.info("Successfully locked user: {}", username);
        } catch (Exception e) {
            log.error("Error locking user {}: {}", username, e.getMessage());
            throw e;
        }
        return "redirect:/admin/reports";
    }



    @PostMapping("/delete-report")
    public String deleteReport(@RequestParam Long id) {
        reportService.deleteReport(id);
        return "redirect:/admin/reports";
    }


    @PostMapping("/delete")
    public String deletePost(@RequestParam Long id) {
        postService.deletePostAdmin(id);
        return "redirect:/admin/reports";

    }


    @PostMapping("/post")
    public String createReport(@Valid @ModelAttribute("reportForm") PostReportCreateDTO reportDTO,
                               BindingResult bindingResult,
                               @AuthenticationPrincipal UserDetails userDetails,
                               RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Пожалуйста, проверьте введенные данные");
            return "redirect:/posts/" + reportDTO.getPostId();
        }

        try {
            reportService.createReport(reportDTO, userDetails.getUsername());
            redirectAttributes.addFlashAttribute("success", "Жалоба успешно отправлена");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка при отправке жалобы");
        }

        return "redirect:/posts/" + reportDTO.getPostId();
    }


}
