package com.example.askfm.service;

import com.example.askfm.dto.MessageDTO;
import com.example.askfm.exception.MessageProcessingException;
import com.example.askfm.model.Message;
import com.example.askfm.model.User;
import com.example.askfm.repository.MessageRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class MessageService {
    private final MessageRepository messageRepository;
    private final SimpMessageSendingOperations messagingTemplate;
    private final ExecutorService messageProcessor;

    private static final int BATCH_SIZE = 10;
    private final BlockingQueue<Message> messageQueue = new ArrayBlockingQueue<>(50);

    @PostConstruct
    public void init() {
        messageProcessor.submit(this::processMessageQueue);
    }

    public void sendMessage(MessageDTO dto) {
        Message message = Message.builder()
                .sender(User.builder().id(dto.getSenderId()).build())
                .recipient(User.builder().id(dto.getRecipientId()).build())
                .content(dto.getContent())
                .timestamp(LocalDateTime.now())
                .read(false)
                .build();

        if (!messageQueue.offer(message)) {
            log.warn("Message queue full, processing synchronously");
            saveAndSendMessage(message);
        }
    }

    @Transactional(readOnly = true)
    public Page<MessageDTO> getRecentMessages(Long userId, int page, int size) {
        return messageRepository.findRecentMessages(userId, PageRequest.of(page, size))
                .map(this::convertToDTO);
    }

    private void processMessageQueue() {
        List<Message> batch = new ArrayList<>(BATCH_SIZE);
        while (!Thread.currentThread().isInterrupted()) {
            try {
                Message message = messageQueue.poll(100, TimeUnit.MILLISECONDS);
                if (message != null) {
                    batch.add(message);
                    sendToUser(message);
                }

                if (batch.size() >= BATCH_SIZE || (!batch.isEmpty() && message == null)) {
                    saveBatchWithRetry(batch);
                    batch.clear();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("Message processing interrupted", e);
                saveBatchWithRetry(batch);
                break;
            }
        }
    }

    @Transactional
    protected void saveBatchWithRetry(List<Message> messages) {
        int attempts = 3;
        while (attempts > 0) {
            try {
                messageRepository.saveAll(messages);
                return;
            } catch (Exception e) {
                attempts--;
                if (attempts == 0) {
                    log.error("Failed to save message batch after 3 attempts", e);
                    messages.forEach(this::saveAndSendMessage);
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }

    private void sendToUser(Message message) {
        try {
            messagingTemplate.convertAndSendToUser(
                    message.getRecipient().getId().toString(),
                    "/queue/messages",
                    convertToDTO(message)
            );
        } catch (Exception e) {
            log.error("Failed to send message to user {}", message.getRecipient().getId(), e);
        }
    }

    @Transactional
    protected void saveAndSendMessage(Message message) {
        try {
            messageRepository.save(message);
            sendToUser(message);
        } catch (Exception e) {
            log.error("Error processing message synchronously", e);
            throw new MessageProcessingException("Failed to process message", e);
        }
    }

    @Transactional(readOnly = true)
    public List<MessageDTO> getUserConversations(Long userId) {
        return messageRepository.findConversations(userId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MessageDTO> getConversationHistory(Long userId, Long recipientId) {
        return messageRepository.findConversationHistory(userId, recipientId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }



    private MessageDTO convertToDTO(Message message) {
        return MessageDTO.builder()
                .id(message.getId())
                .senderId(message.getSender().getId())
                .recipientId(message.getRecipient().getId())
                .content(message.getContent())
                .timestamp(message.getTimestamp())
                .read(message.isRead())
                .build();
    }
}