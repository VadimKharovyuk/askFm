package com.example.askfm.controller;

import com.example.askfm.dto.SupportTicketDto;
import com.example.askfm.enums.TicketStatus;
import com.example.askfm.service.SupportTicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/support")
@RequiredArgsConstructor
public class SupportTicketController {
    private final SupportTicketService supportTicketService;

    @GetMapping()
    public String showCreateForm(Model model) {
        model.addAttribute("ticketDto", new SupportTicketDto());
        return "tickets/create";
    }

    @PostMapping
    public String createTicket(@ModelAttribute SupportTicketDto dto) {
        supportTicketService.createTicket(dto);
        return "tickets/success";
    }

}