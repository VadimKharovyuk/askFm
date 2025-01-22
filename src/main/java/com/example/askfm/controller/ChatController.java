package com.example.askfm.controller;

import com.example.askfm.dto.MessageDTO;
import com.example.askfm.model.User;
import com.example.askfm.service.MessageService;
import com.example.askfm.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.http.ResponseEntity;

import java.util.List;
@Controller
@RequestMapping("/messages")
@RequiredArgsConstructor
public class ChatController {
    private final MessageService messageService;
    private final UserService userService;

    @GetMapping
    public String messagesPage(Model model,
                               @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = userService.findByUsername(userDetails.getUsername()).getId();
        model.addAttribute("userId", userId);
        model.addAttribute("conversations", messageService.getUserConversations(userId));
        return "chat/index";
    }

    @GetMapping("/chat")
    public String chatPage(Model model,
                           @AuthenticationPrincipal UserDetails userDetails,
                           @RequestParam Long recipientId) {
        User currentUser = userService.findByUsername(userDetails.getUsername());
        User recipient = userService.findById(recipientId);

        model.addAttribute("userId", currentUser.getId());
        model.addAttribute("recipientId", recipientId);
        model.addAttribute("recipientName", recipient.getUsername());
        model.addAttribute("messageHistory",
                messageService.getConversationHistory(currentUser.getId(), recipientId));

        return "chat/chat";
    }

    @MessageMapping("/send")
    @SendToUser("/queue/messages")
    public void sendMessage(@Payload MessageDTO message) {
        messageService.sendMessage(message);
    }

    @GetMapping("/api/messages")
    public ResponseEntity<Page<MessageDTO>> getMessages(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(messageService.getRecentMessages(userId, page, size));
    }

}
