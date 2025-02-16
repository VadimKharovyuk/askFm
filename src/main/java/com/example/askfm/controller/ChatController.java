package com.example.askfm.controller;

import com.example.askfm.dto.MessageDTO;
import com.example.askfm.model.User;
import com.example.askfm.service.MessageService;
import com.example.askfm.service.NotificationService;
import com.example.askfm.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.http.ResponseEntity;

import java.security.Principal;
import java.util.List;
@Slf4j
@Controller
@RequestMapping("/messages")
@RequiredArgsConstructor
public class ChatController {
    private final MessageService messageService;
    private final UserService userService;
    private final NotificationService notificationService;


    @GetMapping
    public String messagesPage(Model model,
                               @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = userService.findByUsername(userDetails.getUsername()).getId();
        model.addAttribute("userId", userId);
        model.addAttribute("currentUser", userDetails.getUsername());
        model.addAttribute("conversations", messageService.getUserConversations(userId));
        String currentUsername = userDetails.getUsername();
        // Добавляем разные атрибуты для сообщений и уведомлений
        if (currentUsername != null) {
            long unreadMessages = messageService.getUnreadCount(currentUsername);
            long unreadNotifications = notificationService.getUnreadCount(currentUsername);

            model.addAttribute("unreadMessagesCount", unreadMessages);
            model.addAttribute("unreadCount", unreadNotifications);
        }

        return "chat/index";
    }
    @GetMapping("/chat")
    public String chatPage(Model model,
                           @AuthenticationPrincipal UserDetails userDetails,
                           @RequestParam Long recipientId) {
        User currentUser = userService.findByUsername(userDetails.getUsername());
        User recipient = userService.findById(recipientId);

        // Отмечаем сообщения как прочитанные
        messageService.markConversationAsRead(currentUser.getId(), recipientId);

        model.addAttribute("userId", currentUser.getId());
        // Получаем обновленное количество непрочитанных сообщений
        long unreadMessages = messageService.getUnreadCount(userDetails.getUsername());
        long unreadNotifications = notificationService.getUnreadCount(currentUser.getUsername());

        model.addAttribute("unreadMessagesCount", unreadMessages);
        model.addAttribute("unreadCount", unreadNotifications);
        model.addAttribute("currentUser", currentUser.getUsername());
        model.addAttribute("recipientId", recipientId);
        model.addAttribute("recipientName", recipient.getUsername());
        model.addAttribute("messageHistory",
                messageService.getConversationHistory(currentUser.getId(), recipientId, 0, 50));

        return "chat/chat";
    }


    @GetMapping("/api/messages/history")
    @ResponseBody
    public List<MessageDTO> getMoreMessages(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam Long recipientId,
            @RequestParam int page,
            @RequestParam(defaultValue = "50") int size) {
        User currentUser = userService.findByUsername(userDetails.getUsername());
        return messageService.getConversationHistory(currentUser.getId(), recipientId, page, size);
    }

    @MessageMapping("/send")
    @SendToUser("/queue/messages")
    public void sendMessage(@Payload @Valid MessageDTO message, Principal principal) {
        // Проверяем, что отправитель совпадает с аутентифицированным пользователем
        UserDetails userDetails = (UserDetails) ((Authentication) principal).getPrincipal();
        User sender = userService.findByUsername(userDetails.getUsername());

        // Устанавливаем senderId из аутентифицированного пользователя
        message.setSenderId(sender.getId());

        log.debug("Received message: {}", message);
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
