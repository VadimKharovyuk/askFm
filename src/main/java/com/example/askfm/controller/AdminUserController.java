package com.example.askfm.controller;

import com.example.askfm.enums.UserRole;
import com.example.askfm.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminUserController {
    private final UserService userService;

    // Страница управления пользователями
    @GetMapping("/users")
    public String userManagement(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        model.addAttribute("roles", UserRole.values());
        return "admin/users";
    }

    // Изменение роли пользователя
    @PostMapping("/users/{username}/role")
    public String updateUserRole(@PathVariable String username,
                                 @RequestParam UserRole newRole) {
        try {
            userService.updateUserRole(username, newRole);
            return "redirect:/admin/users?success=Role updated successfully";
        } catch (Exception e) {
            return "redirect:/admin/users?error=" + e.getMessage();
        }
    }

    // Поиск пользователей
    @GetMapping("/users/search")
    public String searchUsers(@RequestParam String query, Model model) {
        model.addAttribute("users", userService.searchUsers(query));
        model.addAttribute("roles", UserRole.values());
        return "admin/users";
    }
}
