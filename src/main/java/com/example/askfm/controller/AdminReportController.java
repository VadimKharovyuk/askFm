package com.example.askfm.controller;

import com.example.askfm.dto.PostReportDTO;
import com.example.askfm.service.PostReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/admin/reports")
@RequiredArgsConstructor
public class AdminReportController {
    private final PostReportService postReportService;

    @GetMapping
    public String getAllReports(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<PostReportDTO> reports = postReportService.getAllReports(pageable);

        model.addAttribute("reports", reports);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", reports.getTotalPages());

        return "admin/reports";
    }
}
