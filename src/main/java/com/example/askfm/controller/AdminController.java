package com.example.askfm.controller;

import com.example.askfm.dto.AdminLoginDTO;
import com.example.askfm.dto.AdminRegistrationDTO;
import com.example.askfm.enums.AdminRole;
import com.example.askfm.model.Admin;
import com.example.askfm.service.AdminService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
@Controller
@RequiredArgsConstructor
public class AdminController {
    private final AdminService adminService;



    @GetMapping("/admin/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("admin", new AdminRegistrationDTO());
        return "admin/register";
    }

    @PostMapping("/admin/register")
    public String registerAdmin(@Valid @ModelAttribute("admin") AdminRegistrationDTO registrationDTO,
                                BindingResult result) {
        if (result.hasErrors()) {
            return "admin/register";
        }

        try {
            adminService.registerAdmin(registrationDTO);
            return "redirect:/admin/login";
        } catch (IllegalArgumentException e) {
            result.rejectValue("username", "error.user", e.getMessage());
            return "admin/register";
        }
    }
    @GetMapping("/admin/login")
    public String showLoginForm(Model model) {
        model.addAttribute("adminLoginDTO", new AdminLoginDTO());
        return "admin/login";
    }

    @PostMapping("/admin/login")
    public String loginAdmin(@Valid @ModelAttribute("adminLoginDTO") AdminLoginDTO loginDTO,
                             BindingResult result,
                             HttpSession session) {
        if (result.hasErrors()) {
            return "admin/login";
        }

        try {
            Admin admin = adminService.authenticateAdmin(loginDTO.getUsername(), loginDTO.getPassword());
            session.setAttribute("adminId", admin.getId());
            session.setAttribute("adminUsername", admin.getUsername());
            return "redirect:/admin/dashboard";
        } catch (IllegalArgumentException e) {
            result.rejectValue("username", "error.login", "Неверное имя пользователя или пароль");
            return "admin/login";
        }
    }

    @GetMapping("/admin/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/admin/login";
    }

    @GetMapping("/admin/dashboard")
    public String dashboard(HttpSession session, Model model) {
        Long adminId = (Long) session.getAttribute("adminId");
        if (adminId == null) {
            return "redirect:/admin/login";
        }

       model.addAttribute("totalUsers",adminService.getTotalUsersCount());
        return "admin/dashboard";
    }
}