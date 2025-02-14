package com.example.askfm.service;

import com.example.askfm.EventListener.*;
import com.example.askfm.dto.NotificationDTO;
import com.example.askfm.enums.NotificationType;
import com.example.askfm.exception.NotificationNotFoundException;
import com.example.askfm.exception.UnauthorizedException;
import com.example.askfm.maper.NotificationMapper;
import com.example.askfm.model.*;
import com.example.askfm.repository.NotificationRepository;
import com.example.askfm.repository.SubscriptionRepository;
import com.example.askfm.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;

    public List<NotificationDTO> getUserNotifications(String username) {
        log.debug("Получение уведомлений для пользователя: {}", username);
        List<Notification> notifications = notificationRepository
                .findByUserUsernameOrderByCreatedAtDesc(username);
        return notificationMapper.toDtoList(notifications);
    }

    @Transactional
    public void deleteAllUserNotifications(String username) {
        log.debug("Удаление всех уведомлений пользователя: {}", username);
        notificationRepository.deleteByUserUsername(username);
        log.info("Все уведомления пользователя {} удалены", username);
    }

    @Transactional
    public void markAsRead(Long notificationId, String username) {
        log.debug("Отметка уведомления как прочитанного: {}", notificationId);

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NotificationNotFoundException(notificationId));

        if (!notification.getUser().getUsername().equals(username)) {
            log.error("Попытка отметить чужое уведомление: {}", notificationId);
            throw new UnauthorizedException("Cannot mark another user's notification as read");
        }

        notification.setRead(true);
        notificationRepository.save(notification);
        log.info("Уведомление {} отмечено как прочитанное", notificationId);
    }

    public long getUnreadCount(String username) {
        return notificationRepository.countByUserUsernameAndIsReadFalse(username);
    }

    public void notifyAboutPhotoEvent(User buyer, Photo photo) {
        log.debug("🚀 Публикация события о покупке фото: buyer={}, photoId={}",
                buyer.getUsername(), photo.getId());

        try {
            eventPublisher.publishEvent(new PhotoPurchaseEvent(buyer, photo));
            log.debug("✅ Событие о покупке фото успешно опубликовано");
        } catch (Exception e) {
            log.error("❌ Ошибка при публикации события о покупке фото: {}", e.getMessage());
            throw e;
        }
    }

    public void notifyAboutLike(User liker, Post post) {

        eventPublisher.publishEvent(new LikeEvent(liker, post));
    }

    public void notifyAboutComment(User commenter, Post post) {
        log.debug("🚀 Публикация события комментария от {} для поста {}",
                commenter.getUsername(), post.getId());
        eventPublisher.publishEvent(new CommentEvent(commenter, post));
    }

    public void notifyAboutSubscription(User subscriber, User targetUser) {
        log.debug("🚀 Публикация события подписки: {} подписался на {}",
                subscriber.getUsername(), targetUser.getUsername());

        try {
            eventPublisher.publishEvent(new SubscriptionEvent(subscriber, targetUser));
            log.info("✅ Успешно опубликовано событие подписки: {} → {}",
                    subscriber.getUsername(), targetUser.getUsername());
        } catch (Exception e) {
            log.error("❌ Ошибка при публикации события подписки {} на {}: {}",
                    subscriber.getUsername(), targetUser.getUsername(), e.getMessage());
            throw e;
        }
    }

    public void notifyAboutRepost(User user, Repost repost) {
        try {
            eventPublisher.publishEvent(new RepostEvent(user, repost));
        } catch (Exception e) {
            log.error("Failed to publish repost event: user={}, repost={}", user.getUsername(), repost.getId(), e);
            throw new NotificationNotFoundException(repost.getId());
        }
    }

    @Transactional
    public void deleteNotification(Long notificationId, String username) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NotificationNotFoundException(notificationId));

        // Проверяем, что уведомление принадлежит пользователю
        if (!notification.getUser().getUsername().equals(username)) {
            throw new UnauthorizedException("Cannot delete another user's notification");
        }

        notificationRepository.delete(notification);
        log.debug("✅ Уведомление {} удалено", notificationId);
    }


    public void notifyAboutEventCreation(Event event, User creator) {
        log.debug("🚀 Публикация создания события: {} создал событие {}",
                creator.getUsername(), event.getTitle());

        try {
            EventEvent eventEvent = EventEvent.createEvent(event, creator);
            eventPublisher.publishEvent(eventEvent);
        } catch (Exception e) {
            log.error("❌ Ошибка при публикации создания события {} от {}: {}",
                    event.getTitle(), creator.getUsername(), e.getMessage());
            throw e;
        }
    }

    public void notifyAboutEventUpdate(Event event, User updater) {
        log.debug("🚀 Публикация обновления события: {} обновил событие {}",
                updater.getUsername(), event.getTitle());

        try {
            EventEvent eventEvent = EventEvent.updateEvent(event, updater);
            eventPublisher.publishEvent(eventEvent);
        } catch (Exception e) {
            log.error("❌ Ошибка при публикации обновления события {} от {}: {}",
                    event.getTitle(), updater.getUsername(), e.getMessage());
            throw e;
        }
    }


    @Transactional
    public void createCancelEventNotifications(Event event, User canceller) {
        List<Long> subscriberIds = subscriptionRepository.findSubscriberIdsBySubscribedToId(event.getCreator().getId());

        if (!subscriberIds.isEmpty()) {
            LocalDateTime now = LocalDateTime.now();
            String message = canceller.getUsername() + " " + NotificationType.EVENT_CANCELLED.getActionMessage();

            // Сохраняем важную информацию о событии перед удалением
            String eventTitle = event.getTitle();
            Long eventId = event.getId();

            for (Long subscriberId : subscriberIds) {
                try {
                    Notification notification = Notification.builder()
                            .user(userRepository.getReferenceById(subscriberId))
                            .initiator(canceller)
                            .type(NotificationType.EVENT_CANCELLED)  // Явно указываем тип
                            .message(message)
                            .createdAt(now)
                            .isRead(false)
                            .build();

                    notificationRepository.save(notification);

                    log.debug("📨 Создано уведомление об отмене события для подписчика {}", subscriberId);
                } catch (Exception e) {
                    log.error("❌ Ошибка при создании уведомления для подписчика {}: {}", subscriberId, e.getMessage());
                }
            }

            log.info("✅ Созданы уведомления об отмене события '{}' для {} подписчиков",
                    eventTitle, subscriberIds.size());
        }
    }
}
