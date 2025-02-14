package com.example.askfm.EventListener;

import com.example.askfm.dto.NotificationDTO;
import com.example.askfm.dto.UserSearchDTO;
import com.example.askfm.enums.NotificationType;
import com.example.askfm.exception.NotificationProcessingException;
import com.example.askfm.maper.NotificationMapper;
import com.example.askfm.model.*;
import com.example.askfm.repository.EventRepository;
import com.example.askfm.repository.NotificationRepository;
import com.example.askfm.repository.SubscriptionRepository;
import com.example.askfm.repository.UserRepository;
import com.example.askfm.service.NotificationService;
import com.example.askfm.service.SubscriptionService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationListener {
    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
  private final SubscriptionRepository subscriptionRepository;



    @EventListener
    @Async("eventExecutor")
    @Transactional
    public void handleEventCreated(EventEvent eventEvent) {
        // Проверяем, что это событие создания
        if (eventEvent.getType() != NotificationType.EVENT_CREATED) {
            return;
        }

        try {
            Event event = eventRepository.findByIdWithCreator(eventEvent.getEventId())
                    .orElseThrow(() -> new EntityNotFoundException("Event not found with id: " + eventEvent.getEventId()));

            User creator = userRepository.findByIdEager(eventEvent.getCreatorId())
                    .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + eventEvent.getCreatorId()));

            log.debug("📝 Обработка создания события от {} для события {}",
                    creator.getUsername(), event.getId());

            // Получаем ID всех подписчиков
            List<Long> subscriberIds = subscriptionRepository.findSubscriberIdsBySubscribedToId(creator.getId());

            if (!subscriberIds.isEmpty()) {
                // Формируем параметры для batch insert
                LocalDateTime now = LocalDateTime.now();
                String message = creator.getUsername() + " " + NotificationType.EVENT_CREATED.getActionMessage();

                // Обрабатываем по частям
                int batchSize = 1000;
                for (int i = 0; i < subscriberIds.size(); i += batchSize) {
                    List<Long> batch = subscriberIds.subList(
                            i, Math.min(subscriberIds.size(), i + batchSize)
                    );

                    for (Long subscriberId : batch) {
                        notificationRepository.batchCreateEventNotifications(
                                subscriberId,
                                creator.getId(),
                                event.getId(),
                                NotificationType.EVENT_CREATED.name(),
                                message,
                                now
                        );
                    }

                    log.debug("📨 Создана пачка уведомлений о событии ({} из {})",
                            Math.min(i + batchSize, subscriberIds.size()),
                            subscriberIds.size());
                }
            }

            log.info("✅ Созданы уведомления о событии для {} подписчиков", subscriberIds.size());

        } catch (Exception e) {
            log.error("❌ Неожиданная ошибка при создании уведомлений: {}", e.getMessage());
            throw new NotificationProcessingException("Неожиданная ошибка: " + e.getMessage());
        }
    }

    @EventListener
    @Async("eventExecutor")
    @Transactional
    public void handleEventUpdated(EventEvent eventEvent) {
        // Проверяем, что это событие обновления
        if (eventEvent.getType() != NotificationType.EVENT_UPDATED) {
            return;
        }

        try {
            Event event = eventRepository.findByIdWithCreator(eventEvent.getEventId())
                    .orElseThrow(() -> new EntityNotFoundException("Event not found with id: " + eventEvent.getEventId()));

            User updater = userRepository.findByIdEager(eventEvent.getCreatorId())
                    .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + eventEvent.getCreatorId()));

            log.debug("📝 Обработка обновления события от {} для события {}",
                    updater.getUsername(), event.getId());

            // Получаем ID всех подписчиков
            List<Long> subscriberIds = subscriptionRepository.findSubscriberIdsBySubscribedToId(updater.getId());

            if (!subscriberIds.isEmpty()) {
                // Формируем параметры для batch insert
                LocalDateTime now = LocalDateTime.now();
                String message = updater.getUsername() + " " + NotificationType.EVENT_UPDATED.getActionMessage();

                // Обрабатываем по частям
                int batchSize = 1000;
                for (int i = 0; i < subscriberIds.size(); i += batchSize) {
                    List<Long> batch = subscriberIds.subList(
                            i, Math.min(subscriberIds.size(), i + batchSize)
                    );

                    for (Long subscriberId : batch) {
                        notificationRepository.batchCreateEventNotifications(
                                subscriberId,
                                updater.getId(),
                                event.getId(),
                                NotificationType.EVENT_UPDATED.name(),
                                message,
                                now
                        );
                    }

                    log.debug("📨 Создана пачка уведомлений об обновлении события ({} из {})",
                            Math.min(i + batchSize, subscriberIds.size()),
                            subscriberIds.size());
                }
            }

            log.info("✅ Созданы уведомления об обновлении события для {} подписчиков", subscriberIds.size());

        } catch (Exception e) {
            log.error("❌ Неожиданная ошибка при создании уведомлений об обновлении: {}", e.getMessage());
            throw new NotificationProcessingException("Неожиданная ошибка: " + e.getMessage());
        }
    }




    @EventListener
    @Async("notificationExecutor")
    public void handlePhotoBay(PhotoPurchaseEvent event) {
        try {
            log.debug("📸 Обработка события покупки фото от {} для фото {}",
                    event.getBuyer().getUsername(), event.getPhoto().getId());

            Notification notification = Notification.builder()
                    .user(event.getPhoto().getOwner()) // владелец фото получает уведомление
                    .initiator(event.getBuyer())       // покупатель является инициатором
                    .photo(event.getPhoto())           // связываем с фото
                    .type(NotificationType.PHOTO_PURCHASE)
                    .message(String.format("%s купил(-а) вашу фотографию",
                            event.getBuyer().getUsername()))
                    .createdAt(LocalDateTime.now())
                    .isRead(false)
                    .build();

            notificationRepository.save(notification);
            log.debug("✅ Уведомление о покупке фото сохранено в БД");
        } catch (Exception e) {
            log.error("❌ Ошибка при создании уведомления о покупке фото: {}", e.getMessage());
            throw e;
        }
    }

    @EventListener
    @Async("commentExecutor")
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
            throw e;
        }
    }



    @EventListener
    @Async("likeExecutor")
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
    @Async("notificationExecutor")
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


    @EventListener
    @Async("eventExecutor")
    public void handleRepostEvent(RepostEvent repostEvent) {
        try {
            log.debug("📝 Обработка репоста от {} поста {}",
                    repostEvent.getRepostUser().getUsername(),
                    repostEvent.getRepost().getOriginalPost().getId());

            Repost repost = repostEvent.getRepost();
            Notification notification = Notification.builder()
                    .user(repost.getOriginalPost().getAuthor())
                    .initiator(repost.getUser())
                    .post(repost.getOriginalPost())
                    .repost(repost)
                    .type(NotificationType.REPOST)
                    .message(NotificationType.REPOST.getActionMessage())
                    .createdAt(LocalDateTime.now())
                    .isRead(false)
                    .build();

            Notification savedNotification = notificationRepository.save(notification);
            log.info("✅ Создано уведомление о репосте для {}",
                    repost.getOriginalPost().getAuthor().getUsername());

        } catch (Exception e) {
            log.error("❌ Ошибка при создании уведомления о репосте: {}", e.getMessage());
            throw e;
        }
    }
}
