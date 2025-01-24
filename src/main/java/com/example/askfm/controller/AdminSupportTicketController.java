package com.example.askfm.controller;

import com.example.askfm.enums.TicketStatus;
import com.example.askfm.enums.UserRole;
import com.example.askfm.model.User;
import com.example.askfm.service.SupportTicketService;
import com.example.askfm.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/support")
@RequiredArgsConstructor
public class AdminSupportTicketController {
    private final SupportTicketService supportTicketService;
    private final UserService userService;

    @GetMapping
    public String getAllTickets(Model model,@AuthenticationPrincipal UserDetails userDetails) {
        // Получаем пользователя и проверяем его роль
        User currentUser = userService.findByUsername(userDetails.getUsername());
        if (currentUser.getRole() != UserRole.ADMIN) {
            return "redirect:/login";
        }
        model.addAttribute("tickets", supportTicketService.getAllTickets());
        return "admin/tickets/list";
    }

    @GetMapping("/{id}")
    public String getTicketDetails(@PathVariable Long id, Model model) {
        model.addAttribute("ticket", supportTicketService.getTicketById(id));
        model.addAttribute("statuses", TicketStatus.values());
        return "admin/tickets/details";
    }

    @PostMapping("/{id}/status")
    public String updateStatus(@PathVariable Long id, @RequestParam TicketStatus status) {
        supportTicketService.updateStatus(id, status);
        return "redirect:/admin/support/" + id;
    }
}