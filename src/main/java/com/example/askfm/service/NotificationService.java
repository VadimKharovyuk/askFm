package com.example.askfm.service;

import com.example.askfm.EventListener.CommentEvent;
import com.example.askfm.EventListener.LikeEvent;
import com.example.askfm.EventListener.RepostEvent;
import com.example.askfm.EventListener.SubscriptionEvent;
import com.example.askfm.dto.NotificationDTO;
import com.example.askfm.exception.NotificationNotFoundException;
import com.example.askfm.exception.UnauthorizedException;
import com.example.askfm.maper.NotificationMapper;
import com.example.askfm.model.Notification;
import com.example.askfm.model.Post;
import com.example.askfm.model.Repost;
import com.example.askfm.model.User;
import com.example.askfm.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;
    private final ApplicationEventPublisher eventPublisher;

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

}
