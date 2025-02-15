package com.example.askfm.controller;

import com.example.askfm.dto.AdminUserSearchDTO;
import com.example.askfm.dto.MessageDTO;
import com.example.askfm.model.User;
import com.example.askfm.service.AdminChatMonitoringService;
import com.example.askfm.service.UserService;
import com.example.askfm.maper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.nio.file.AccessDeniedException;
import java.util.List;

@Slf4j
@Controller
@RequestMapping("/admin/chats")
@RequiredArgsConstructor
public class AdminChatController {
    private final AdminChatMonitoringService adminChatService;
    private final UserService userService;
    private final UserMapper userMapper;

    @GetMapping
    public String showAdminPanel(
            @AuthenticationPrincipal UserDetails adminDetails,
            @RequestParam(required = false) String query,
            Model model) {
        User admin = userService.findByUsername(adminDetails.getUsername());
        model.addAttribute("adminName", admin.getUsername());

        if (query != null && !query.trim().isEmpty()) {
            List<AdminUserSearchDTO> users = userMapper.toAdminSearchDTOList(
                    userService.searchUsers(query.trim())
            );
            model.addAttribute("users", users);
            model.addAttribute("searchQuery", query);
        }
        return "admin/chat-monitoring";
    }
 //done
    @GetMapping("/user/{username}/messages")
    public String getUserMessages(
            @AuthenticationPrincipal UserDetails adminDetails,
            @PathVariable String username,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Model model) throws AccessDeniedException {
        log.debug("Getting messages for username: {}, page: {}, size: {}", username, page, size);

        User admin = userService.findByUsername(adminDetails.getUsername());
        log.debug("Found admin user: {}", admin.getUsername());

        Page<MessageDTO> messages = adminChatService.getUserMessages(admin, username, page, size);
        log.debug("Retrieved {} messages", messages.getTotalElements());

        model.addAttribute("messages", messages.getContent());  // Важно! Добавляем .getContent()
        model.addAttribute("currentUser", username);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", messages.getTotalPages());

        return "admin/chat-monitoring";
    }

    @GetMapping("/conversation")
    public String viewConversation(
            @AuthenticationPrincipal UserDetails adminDetails,
            @RequestParam String user1,
            @RequestParam String user2,
            Model model) throws AccessDeniedException {
        User admin = userService.findByUsername(adminDetails.getUsername());
        List<MessageDTO> messages = adminChatService.getChatHistory(admin, user1, user2);
        model.addAttribute("messages", messages);
        model.addAttribute("user1", user1);
        model.addAttribute("user2", user2);
        return "admin/chat-monitoring";
    }

    @GetMapping("/user/{username}/active-chats")
    public String getUserActiveChats(
            @AuthenticationPrincipal UserDetails adminDetails,
            @PathVariable String username,
            Model model) throws AccessDeniedException {
        User admin = userService.findByUsername(adminDetails.getUsername());
        List<MessageDTO> activeChats = adminChatService.getUserActiveChats(admin, username);
        model.addAttribute("activeChats", activeChats);
        model.addAttribute("username", username);
        return "admin/chat-monitoring";
    }

    @PostMapping("/message/{messageId}/delete")
    public String deleteMessage(
            @AuthenticationPrincipal UserDetails adminDetails,
            @PathVariable Long messageId,
            RedirectAttributes redirectAttributes) throws AccessDeniedException {
        try {
            User admin = userService.findByUsername(adminDetails.getUsername());
//            adminChatService.deleteMessage(admin, messageId);
            redirectAttributes.addFlashAttribute("successMessage", "Message successfully deleted");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting message: " + e.getMessage());
        }
        return "redirect:/admin/chats";
    }

    @PostMapping("/user/{username}/block")
    public String blockUser(
            @AuthenticationPrincipal UserDetails adminDetails,
            @PathVariable String username,
            @RequestParam(required = false) String reason,
            RedirectAttributes redirectAttributes) throws AccessDeniedException {
        try {
            User admin = userService.findByUsername(adminDetails.getUsername());
//            adminChatService.blockUser(admin, username, reason);
            redirectAttributes.addFlashAttribute("successMessage",
                    "User " + username + " has been blocked");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Error blocking user: " + e.getMessage());
        }
        return "redirect:/admin/chats";
    }

    @PostMapping("/user/{username}/unblock")
    public String unblockUser(
            @AuthenticationPrincipal UserDetails adminDetails,
            @PathVariable String username,
            RedirectAttributes redirectAttributes) throws AccessDeniedException {
        try {
            User admin = userService.findByUsername(adminDetails.getUsername());
//            adminChatService.unblockUser(admin, username);
            redirectAttributes.addFlashAttribute("successMessage",
                    "User " + username + " has been unblocked");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Error unblocking user: " + e.getMessage());
        }
        return "redirect:/admin/chats";
    }


    @GetMapping("/search")
    public String searchMessages(
            @AuthenticationPrincipal UserDetails adminDetails,
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Model model) throws AccessDeniedException {
        log.debug("Searching messages with keyword: {}, page: {}, size: {}", keyword, page, size);

        User admin = userService.findByUsername(adminDetails.getUsername());

        Page<MessageDTO> messages = adminChatService.searchMessages(admin, keyword, page, size);

        model.addAttribute("messages", messages.getContent());
        model.addAttribute("keyword", keyword);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", messages.getTotalPages());
        model.addAttribute("totalElements", messages.getTotalElements());
        model.addAttribute("isSearchResult", true);

        return "admin/chat-monitoring";
    }
    @ExceptionHandler(AccessDeniedException.class)
    public String handleAccessDenied(
            AccessDeniedException e,
            RedirectAttributes redirectAttributes) {
        log.warn("Access denied: {}", e.getMessage());
        redirectAttributes.addFlashAttribute("errorMessage",
                "Access denied: " + e.getMessage());
        return "redirect:/admin/chats";
    }
}