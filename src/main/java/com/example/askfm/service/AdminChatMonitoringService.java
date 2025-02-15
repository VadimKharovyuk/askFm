package com.example.askfm.service;

import com.example.askfm.dto.MessageDTO;
import com.example.askfm.enums.UserRole;
import com.example.askfm.model.Message;
import com.example.askfm.model.User;
import com.example.askfm.repository.MessageRepository;
import com.example.askfm.repository.UserRepository;
import com.example.askfm.maper.MessageMapper;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.AccessDeniedException;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminChatMonitoringService {
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final MessageMapper messageMapper;

    /**
     * Проверяет, имеет ли пользователь права администратора
     */
    private void validateAdminAccess(User admin) throws AccessDeniedException {
        if (admin.getRole() != UserRole.ADMIN) {
            throw new AccessDeniedException("Only administrators can access this functionality");
        }
    }

    /**
     * Получает все сообщения конкретного пользователя
     */
    public Page<MessageDTO> getUserMessages(User admin, String username, int page, int size) throws AccessDeniedException {
        validateAdminAccess(admin);

        User targetUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + username));

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp"));

        Page<Message> messages = messageRepository.findByRecipientOrSender(
                targetUser.getId(),
                pageRequest
        );

        return messageMapper.toDtoPage(messages);
    }

    /**
     * Получает историю переписки между двумя пользователями
     */
    public List<MessageDTO> getChatHistory(User admin, String user1Username, String user2Username) throws AccessDeniedException {
        validateAdminAccess(admin);

        User user1 = userRepository.findByUsername(user1Username)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + user1Username));
        User user2 = userRepository.findByUsername(user2Username)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + user2Username));

        List<Message> messages = messageRepository.findConversationBetweenUsers(
                user1.getId(),
                user2.getId()
        );

        return messageMapper.toDtoList(messages);
    }

    /**
     * Получает список всех активных чатов пользователя
     */
    public List<MessageDTO> getUserActiveChats(User admin, String username) throws AccessDeniedException {
        log.debug("Getting active chats for user: {}", username);

        validateAdminAccess(admin);
        log.debug("Admin access validated for: {}", admin.getUsername());

        User targetUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + username));
        log.debug("Found target user with ID: {}", targetUser.getId());

        List<Message> lastMessages = messageRepository.findConversations(targetUser.getId());
        log.debug("Found {} active conversations", lastMessages.size());

        List<MessageDTO> messageDTOs = messageMapper.toDtoList(lastMessages);
        log.debug("Mapped to DTOs, returning {} conversations", messageDTOs.size());

        return messageDTOs;
    }

    /**
     * Поиск сообщений по содержимому
     */
    public Page<MessageDTO> searchMessages(User admin, String keyword, int page, int size) throws AccessDeniedException {
        validateAdminAccess(admin);

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp"));
        Page<Message> messages = messageRepository.findByContentContainingIgnoreCase(keyword, pageRequest);

        return messageMapper.toDtoPage(messages);
    }
}