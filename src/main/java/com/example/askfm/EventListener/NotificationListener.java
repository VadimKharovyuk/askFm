package com.example.askfm.EventListener;

import com.example.askfm.dto.NotificationDTO;
import com.example.askfm.enums.NotificationType;
import com.example.askfm.maper.NotificationMapper;
import com.example.askfm.model.Notification;
import com.example.askfm.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationListener {
    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;



    @EventListener
    @Transactional
    public void handleCommentEvent(CommentEvent event) {
        try {
            log.debug("📝 Обработка события комментария от {} для поста {}",
                    event.getCommenter().getUsername(), event.getPost().getId());

            Notification notification = Notification.builder()
                    .user(event.getPost().getAuthor()) // автор поста получает уведомление
                    .initiator(event.getCommenter())   // комментатор является инициатором
                    .post(event.getPost())
                    .type(NotificationType.COMMENT)
                    .message(String.format("%s прокомментировал ваш пост",
                            event.getCommenter().getUsername()))
                    .createdAt(LocalDateTime.now())
                    .isRead(false)
                    .build();

            notificationRepository.save(notification);
            log.debug("✅ Уведомление о комментарии сохранено в БД");
        } catch (Exception e) {
            log.error("❌ Ошибка при создании уведомления о комментарии: {}", e.getMessage());
            throw e; // пробрасываем ошибку дальше
        }
    }



    @EventListener
    @Async
    public void handleLikeEvent(LikeEvent event) {
        try {
            log.debug("📝 Обработка события лайка от {} для поста {}",
                    event.getLiker().getUsername(), event.getPost().getId());

            Notification notification = Notification.builder()
                    .user(event.getPost().getAuthor())
                    .initiator(event.getLiker())
                    .post(event.getPost())
                    .type(NotificationType.LIKE)
                    .message(event.getLiker().getUsername() + " лайкнул ваш пост")
                    .createdAt(LocalDateTime.now())
                    .build();

            Notification savedNotification = notificationRepository.save(notification);

            // Можно использовать DTO если нужно что-то вернуть
            NotificationDTO notificationDTO = notificationMapper.toDto(savedNotification);

            log.info("✅ Создано уведомление о лайке для {}",
                    event.getPost().getAuthor().getUsername());

        } catch (Exception e) {
            log.error("❌ Ошибка при создании уведомления о лайке: {}", e.getMessage());
        }
    }
    @EventListener
    @Async
    public void handleSubscriptionEvent(SubscriptionEvent event) {
        try {
            log.debug("📝 Обработка события подписки от {} на пользователя {}",
                    event.getSubscriber().getUsername(),
                    event.getTargetUser().getUsername());

            Notification notification = Notification.builder()
                    .user(event.getTargetUser())         // уведомление получает тот, на кого подписались
                    .initiator(event.getSubscriber())    // инициатор - тот, кто подписался
                    .type(NotificationType.SUBSCRIPTION)
                    .message(String.format("%s подписался(-ась) на вас",
                            event.getSubscriber().getUsername()))
                    .createdAt(LocalDateTime.now())
                    .isRead(false)
                    .build();

            Notification savedNotification = notificationRepository.save(notification);

            NotificationDTO notificationDTO = notificationMapper.toDto(savedNotification);

            log.info("✅ Создано уведомление о подписке для {}",
                    event.getTargetUser().getUsername());

        } catch (Exception e) {
            log.error("❌ Ошибка при создании уведомления о подписке: {}", e.getMessage());
            throw e;
        }
    }
}
