
package com.example.askfm.service;

import com.example.askfm.dto.CreateRepostRequest;
import com.example.askfm.dto.RepostDTO;
import com.example.askfm.exception.*;
import com.example.askfm.maper.RepostMapper;
import com.example.askfm.model.Post;
import com.example.askfm.model.Repost;
import com.example.askfm.model.User;
import com.example.askfm.repository.NotificationRepository;
import com.example.askfm.repository.PostRepository;
import com.example.askfm.repository.RepostRepository;
import com.example.askfm.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class RepostService {
    private final RepostRepository repostRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final RepostMapper repostMapper;
    private final CacheManager cacheManager;
    private final NotificationService notificationService;
    private final NotificationRepository notificationRepository;

    @Transactional
    public RepostDTO createRepost(Long userId, CreateRepostRequest request) {
        log.debug("📝 Начало создания репоста для пользователя: {}, пост: {}", userId, request.getPostId());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("❌ Пользователь не найден: {}", userId);
                    return new UserNotFoundException("User not found with id: " + userId);
                });

        Post originalPost = postRepository.findById(request.getPostId())
                .orElseThrow(() -> {
                    log.error("❌ Оригинальный пост не найден: {}", request.getPostId());
                    return new PostNotFoundException(request.getPostId());
                });

        validateRepostCreation(user, originalPost);

        Repost repost = buildRepost(user, originalPost);
        Repost savedRepost = repostRepository.save(repost);
        log.debug("✨ Репост успешно сохранен в БД");

        RepostDTO repostDTO = repostMapper.toDto(savedRepost, user.getUsername());

        notificationService.notifyAboutRepost(user,savedRepost);

        // Очищаем кеш
        Cache cache = cacheManager.getCache("posts");
        if (cache != null) {
            cache.clear();
            log.debug("🧹 Кеш постов полностью очищен после создания репоста");
        }

        log.info("✅ Создан репост {} пользователем {}", request.getPostId(), user.getUsername());
        return repostDTO;
    }

    @Transactional
    public void deleteRepost(Long userId, Long postId) {
        log.debug("📝 Начало удаления репоста для пользователя: {}, пост: {}", userId, postId);

        Repost repost = repostRepository.findByUserIdAndOriginalPostId(userId, postId)
                .orElseThrow(() -> {
                    log.error("❌ Репост не найден для пользователя: {}, пост: {}", userId, postId);
                    return new RepostNotFoundException("Repost not found");
                });

        validateRepostDeletion(repost, userId);

        // Delete associated notifications first
        deleteRepostNotifications(repost.getId());

        String username = repost.getUser().getUsername();
        repostRepository.delete(repost);
        log.debug("✨ Репост удален из БД");

        // Очищаем кеш
        Cache cache = cacheManager.getCache("posts");
        if (cache != null) {
            cache.clear();
            log.debug("🧹 Кеш постов полностью очищен после удаления репоста");
        }

        log.info("✅ Репост успешно удален пользователем: {}", username);
    }

    /**
     * Delete all notifications associated with a repost
     */
    private void deleteRepostNotifications(Long repostId) {
        log.debug("🔔 Удаление уведомлений, связанных с репостом ID: {}", repostId);
        try {
            // Assuming you have a notificationRepository
            notificationRepository.deleteByRepostId(repostId);
            log.debug("✅ Уведомления, связанные с репостом, успешно удалены");
        } catch (Exception e) {
            log.error("❌ Ошибка при удалении уведомлений, связанных с репостом: {}", e.getMessage(), e);
            throw e;
        }
    }
//    @Transactional
//    public void deleteRepost(Long userId, Long postId) {
//        log.debug("📝 Начало удаления репоста для пользователя: {}, пост: {}", userId, postId);
//
//        Repost repost = repostRepository.findByUserIdAndOriginalPostId(userId, postId)
//                .orElseThrow(() -> {
//                    log.error("❌ Репост не найден для пользователя: {}, пост: {}", userId, postId);
//                    return new RepostNotFoundException("Repost not found");
//                });
//
//        validateRepostDeletion(repost, userId);
//
//        String username = repost.getUser().getUsername();
//        repostRepository.delete(repost);
//        log.debug("✨ Репост удален из БД");
//
//        // Очищаем кеш
//        Cache cache = cacheManager.getCache("posts");
//        if (cache != null) {
//            cache.clear();
//            log.debug("🧹 Кеш постов полностью очищен после удаления репоста");
//        }
//
//        log.info("✅ Репост успешно удален пользователем: {}", username);
//    }
    private void validateRepostCreation(User user, Post originalPost) {
        if (repostRepository.existsByUserAndOriginalPost(user, originalPost)) {
            log.error("❌ Репост уже существует для пользователя: {}, пост: {}",
                    user.getId(), originalPost.getId());
            throw new UnauthorizedException("Repost already exists");
        }
    }

    private void validateRepostDeletion(Repost repost, Long userId) {
        if (!repost.getUser().getId().equals(userId)) {
            log.error("❌ Попытка удаления чужого репоста. UserId: {}, RepostUserId: {}",
                    userId, repost.getUser().getId());
            throw new UnauthorizedException("Cannot delete another user's repost");
        }
    }

    private Repost buildRepost(User user, Post originalPost) {
        return Repost.builder()
                .user(user)
                .originalPost(originalPost)
                .repostedAt(LocalDateTime.now())
                .build();
    }

}