package com.example.askfm.service;

import com.example.askfm.dto.MessageDTO;
import com.example.askfm.exception.MessageProcessingException;
import com.example.askfm.maper.MessageMapper;
import com.example.askfm.model.Message;
import com.example.askfm.model.User;
import com.example.askfm.repository.MessageRepository;
import com.example.askfm.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
    private final UserRepository userRepository;
    private final SimpMessageSendingOperations messagingTemplate;
    private final ExecutorService messageProcessor;
    private final TransactionTemplate transactionTemplate;
    private final MessageMapper messageMapper;

    private static final int BATCH_SIZE = 10;
    private final BlockingQueue<MessageQueueItem> messageQueue = new ArrayBlockingQueue<>(50);

    @PostConstruct
    public void init() {
        messageProcessor.submit(this::processMessageQueue);
    }
    private void processMessageQueue() {
        List<MessageQueueItem> batch = new ArrayList<>(BATCH_SIZE);
        boolean wasEmpty = true; // флаг для отслеживания состояния очереди

        while (!Thread.currentThread().isInterrupted()) {
            try {
                MessageQueueItem item = messageQueue.poll(100, TimeUnit.MILLISECONDS);

                if (item != null) {
                    if (wasEmpty) {
                        log.debug("Started receiving messages");
                        wasEmpty = false;
                    }
                    log.debug("Received message item: {}", item);
                    batch.add(item);
                } else {
                    if (!wasEmpty) {
                        log.debug("Queue is empty now");
                        wasEmpty = true;
                    }
                }

                if (batch.size() >= BATCH_SIZE || (!batch.isEmpty() && messageQueue.isEmpty())) {
                    log.debug("Processing batch of {} messages", batch.size());
                    saveAndSendMessages(batch);
                    batch.clear();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("Message processor interrupted", e);
                break;
            } catch (Exception e) {
                log.error("Error processing message batch: {}", e.getMessage(), e);
                batch.clear();
            }
        }
    }

    @Data
    @AllArgsConstructor
    private static class MessageQueueItem {
        private final Message message;
        private final String recipientUsername;
    }

    public void sendMessage(MessageDTO dto) {
        log.debug("Processing message DTO: {}", dto);

        if (dto.getSenderId() == null || dto.getRecipientId() == null) {
            throw new IllegalArgumentException("Sender and recipient IDs must not be null");
        }

        try {
            log.debug("Finding sender with ID: {}", dto.getSenderId());
            User sender = userRepository.findById(dto.getSenderId())
                    .orElseThrow(() -> new EntityNotFoundException("Sender not found"));

            log.debug("Finding recipient with ID: {}", dto.getRecipientId());
            User recipient = userRepository.findById(dto.getRecipientId())
                    .orElseThrow(() -> new EntityNotFoundException("Recipient not found"));

            String recipientUsername = recipient.getUsername();
            log.debug("Recipient username: {}", recipientUsername);

            Message message = Message.builder()
                    .sender(sender)
                    .recipient(recipient)
                    .content(dto.getContent())
                    .timestamp(LocalDateTime.now())
                    .read(false)
                    .build();
            log.debug("Created message: {}", message);

            MessageQueueItem queueItem = new MessageQueueItem(message, recipientUsername);
            log.debug("Created queue item: {}", queueItem);

            if (!messageQueue.offer(queueItem)) {
                log.warn("Message queue full, processing synchronously");
                saveAndSendMessages(Collections.singletonList(queueItem));
            }
        } catch (Exception e) {
            log.error("Error processing message: {}", e.getMessage(), e);
            throw e;
        }
    }

    private void saveAndSendMessages(List<MessageQueueItem> items) {
        transactionTemplate.execute(status -> {
            try {
                List<Message> messages = items.stream()
                        .map(MessageQueueItem::getMessage)
                        .collect(Collectors.toList());

                List<Message> savedMessages = messageRepository.saveAll(messages);

                for (int i = 0; i < savedMessages.size(); i++) {
                    Message savedMessage = savedMessages.get(i);
                    String recipientUsername = items.get(i).getRecipientUsername();

                    MessageDTO dto = messageMapper.toDto(savedMessage);
                    messagingTemplate.convertAndSendToUser(
                            recipientUsername,
                            "/queue/messages",
                            dto
                    );
                }
                return null;
            } catch (Exception e) {
                log.error("Error in transaction", e);
                throw e;
            }
        });
    }

    @Transactional(readOnly = true)
    public Page<MessageDTO> getRecentMessages(Long userId, int page, int size) {
        return messageMapper.toDtoPage(messageRepository.findRecentMessages(userId, PageRequest.of(page, size)));
    }

    @Transactional(readOnly = true)
    public List<MessageDTO> getUserConversations(Long userId) {
        return messageMapper.toDtoList(messageRepository.findConversations(userId));
    }

    @Transactional(readOnly = true)
    public List<MessageDTO> getConversationHistory(Long userId, Long recipientId, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp"));
        Page<Message> messagePage = messageRepository.findConversationHistory(userId, recipientId, pageRequest);

        return messagePage.getContent().stream()
                .map(messageMapper::toDto)
                .sorted(Comparator.comparing(MessageDTO::getTimestamp))
                .collect(Collectors.toList());
    }


    @Transactional(readOnly = true)
    public long getUnreadCount(String username) {
        return messageRepository.countByRecipientUsernameAndReadFalse(username);
    }

    @Transactional(readOnly = true)
    public long getUnreadCountFromSender(String username, Long senderId) {
        return messageRepository.countByRecipientUsernameAndSenderIdAndReadFalse(username, senderId);
    }

    @Transactional
    public void markConversationAsRead(Long userId, Long senderId) {
        messageRepository.markMessagesAsRead(userId, senderId);
    }

}