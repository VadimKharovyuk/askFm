package com.example.askfm.controller;

import com.example.askfm.dto.PostReportCreateDTO;
import com.example.askfm.service.PostReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/report")
public class ReportController {
    private final PostReportService reportService;


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
