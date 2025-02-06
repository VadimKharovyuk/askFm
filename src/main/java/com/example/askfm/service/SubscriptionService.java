package com.example.askfm.service;//package com.example.askfm.service;
import com.example.askfm.dto.UserSearchDTO;
import com.example.askfm.model.Subscription;
import com.example.askfm.model.User;
import com.example.askfm.repository.SubscriptionRepository;
import com.example.askfm.repository.UserRepository;import com.github.benmanes.caffeine.cache.stats.CacheStats;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
//
//@Slf4j
//@Service
//@Transactional
//@RequiredArgsConstructor
//public class SubscriptionService {
//    private final SubscriptionRepository subscriptionRepository;
//    private final UserRepository userRepository;
//    private final ImageService imageService;
//    private final CacheManager cacheManager;
//
//    @Caching(evict = {
//            @CacheEvict(value = "followers", key = "'subscribers_count:' + #subscribedToUsername"),
//            @CacheEvict(value = "followers", key = "'subscriptions_count:' + #subscriberUsername"),
//            @CacheEvict(value = "followers", key = "'is_following:' + #subscriberUsername + '_' + #subscribedToUsername"),
//            @CacheEvict(value = "followers", key = "'following_users:' + #subscriberUsername"),
//            @CacheEvict(value = "followers", key = "'followers_list:' + #subscribedToUsername")
//    })
//    public void follow(String subscriberUsername, String subscribedToUsername) {
//        log.debug("Creating subscription and clearing cache: {} -> {}", subscriberUsername, subscribedToUsername);
//
//        if (subscriberUsername.equals(subscribedToUsername)) {
//            throw new IllegalArgumentException("Ви не можете підписатися на себе");
//        }
//
//        User subscriber = userRepository.findByUsername(subscriberUsername)
//                .orElseThrow(() -> new UsernameNotFoundException("Користувача не знайдено: " + subscriberUsername));
//
//        User subscribedTo = userRepository.findByUsername(subscribedToUsername)
//                .orElseThrow(() -> new UsernameNotFoundException("Користувача не знайдено: " + subscribedToUsername));
//
//        if (subscriptionRepository.existsBySubscriberAndSubscribedTo(subscriber, subscribedTo)) {
//            throw new IllegalArgumentException("Ви вже підписані на цього користувача");
//        }
//
//        Subscription subscription = Subscription.builder()
//                .subscriber(subscriber)
//                .subscribedTo(subscribedTo)
//                .createdAt(LocalDateTime.now())
//                .build();
//
//        subscriptionRepository.save(subscription);
//        log.debug("Successfully created subscription: {} -> {}", subscriberUsername, subscribedToUsername);
//    }
//
//    @Caching(evict = {
//            @CacheEvict(value = "followers", key = "'subscribers_count:' + #subscribedToUsername"),
//            @CacheEvict(value = "followers", key = "'subscriptions_count:' + #subscriberUsername"),
//            @CacheEvict(value = "followers", key = "'is_following:' + #subscriberUsername + '_' + #subscribedToUsername"),
//            @CacheEvict(value = "followers", key = "'following_users:' + #subscriberUsername"),
//            @CacheEvict(value = "followers", key = "'followers_list:' + #subscribedToUsername")
//    })
//    public void unfollow(String subscriberUsername, String subscribedToUsername) {
//        log.debug("Removing subscription and clearing cache: {} -> {}", subscriberUsername, subscribedToUsername);
//
//        User subscriber = userRepository.findByUsername(subscriberUsername)
//                .orElseThrow(() -> new UsernameNotFoundException("Користувача не знайдено: " + subscriberUsername));
//
//        User subscribedTo = userRepository.findByUsername(subscribedToUsername)
//                .orElseThrow(() -> new UsernameNotFoundException("Користувача не знайдено: " + subscribedToUsername));
//
//        subscriptionRepository.deleteBySubscriberAndSubscribedTo(subscriber, subscribedTo);
//        log.debug("Successfully removed subscription: {} -> {}", subscriberUsername, subscribedToUsername);
//    }
//
//    @Cacheable(
//            value = "followers",
//            key = "'is_following:' + #subscriberUsername + '_' + #subscribedToUsername"
//    )
//    public boolean isFollowing(String subscriberUsername, String subscribedToUsername) {
//        log.debug("Cache miss: Checking if {} is following {}", subscriberUsername, subscribedToUsername);
//
//        if (subscriberUsername.equals(subscribedToUsername)) {
//            return false;
//        }
//
//        User subscriber = userRepository.findByUsername(subscriberUsername)
//                .orElseThrow(() -> new UsernameNotFoundException("Користувача не знайдено: " + subscriberUsername));
//
//        User subscribedTo = userRepository.findByUsername(subscribedToUsername)
//                .orElseThrow(() -> new UsernameNotFoundException("Користувача не знайдено: " + subscribedToUsername));
//
//        return subscriptionRepository.existsBySubscriberAndSubscribedTo(subscriber, subscribedTo);
//    }
//
//    @Cacheable(
//            value = "followers",
//            key = "'subscribers_count:' + #username",
//            unless = "#result == 0"
//    )
//    public long getSubscribersCount(String username) {
//        log.debug("Cache miss: Calculating subscribers count for: {}", username);
//        User user = userRepository.findByUsername(username)
//                .orElseThrow(() -> new UsernameNotFoundException("Користувача не знайдено: " + username));
//        long count = subscriptionRepository.countBySubscribedTo(user);
//        log.debug("Calculated subscribers count for {}: {}", username, count);
//        return count;
//    }
//
//    @Cacheable(
//            value = "followers",
//            key = "'subscriptions_count:' + #username",
//            unless = "#result == 0"
//    )
//    public long getSubscriptionsCount(String username) {
//        log.debug("Cache miss: Calculating subscriptions count for: {}", username);
//        User user = userRepository.findByUsername(username)
//                .orElseThrow(() -> new UsernameNotFoundException("Користувача не знайдено: " + username));
//        long count = subscriptionRepository.countBySubscriber(user);
//        log.debug("Calculated subscriptions count for {}: {}", username, count);
//        return count;
//    }
//
//    @Cacheable(
//            value = "followers",
//            key = "'following_users:' + #username",
//            unless = "#result.isEmpty()"
//    )
//    public List<UserSearchDTO> getFollowingUsers(String username, String currentUsername) {
//        log.debug("Cache miss: Getting following users for: {}", username);
//        User user = userRepository.findByUsername(username)
//                .orElseThrow(() -> new UsernameNotFoundException("Користувача не знайдено: " + username));
//
//        List<UserSearchDTO> results = subscriptionRepository.findBySubscriber(user)
//                .stream()
//                .map(subscription -> mapToUserSearchDTO(subscription.getSubscribedTo(), currentUsername))
//                .collect(Collectors.toList());
//
//        log.debug("Found {} following users for: {}", results.size(), username);
//        return results;
//    }
//
//    @Cacheable(
//            value = "followers",
//            key = "'followers_list:' + #username",
//            unless = "#result.isEmpty()"
//    )
//    public List<UserSearchDTO> getFollowers(String username, String currentUsername) {
//        log.debug("Cache miss: Getting followers for: {}", username);
//        User user = userRepository.findByUsername(username)
//                .orElseThrow(() -> new UsernameNotFoundException("Користувача не знайдено: " + username));
//
//        List<UserSearchDTO> results = subscriptionRepository.findBySubscribedTo(user)
//                .stream()
//                .map(subscription -> mapToUserSearchDTO(subscription.getSubscriber(), currentUsername))
//                .collect(Collectors.toList());
//
//        log.debug("Found {} followers for: {}", results.size(), username);
//        return results;
//    }
//
//    private UserSearchDTO mapToUserSearchDTO(User user, String currentUsername) {
//        return UserSearchDTO.builder()
//                .username(user.getUsername())
//                .avatar(user.getAvatar() != null ?
//                        "data:image/jpeg;base64," + imageService.getBase64Avatar(user.getAvatar()) :
//                        null)
//                .followersCount(getSubscribersCount(user.getUsername()))
//                .isFollowing(currentUsername != null &&
//                        isFollowing(currentUsername, user.getUsername()))
//                .build();
//    }
//
//
//}
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class SubscriptionService {
    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    private final ImageService imageService;
    private final CacheManager cacheManager;

    @Caching(evict = {
            @CacheEvict(value = "followers", key = "'subscribers_count:' + #subscribedToUsername"),
            @CacheEvict(value = "followers", key = "'subscriptions_count:' + #subscriberUsername"),
            @CacheEvict(value = "followers", key = "'is_following:' + #subscriberUsername + '_' + #subscribedToUsername"),
            @CacheEvict(value = "followers", key = "'following_users:' + #subscriberUsername"),
            @CacheEvict(value = "followers", key = "'followers_list:' + #subscribedToUsername")
    })
    public void follow(String subscriberUsername, String subscribedToUsername) {
        log.debug("Створення підписки та очищення кешу: {} -> {}", subscriberUsername, subscribedToUsername);

        if (subscriberUsername.equals(subscribedToUsername)) {
            throw new IllegalArgumentException("Ви не можете підписатися на себе");
        }

        User subscriber = userRepository.findByUsername(subscriberUsername)
                .orElseThrow(() -> new UsernameNotFoundException("Користувача не знайдено: " + subscriberUsername));

        User subscribedTo = userRepository.findByUsername(subscribedToUsername)
                .orElseThrow(() -> new UsernameNotFoundException("Користувача не знайдено: " + subscribedToUsername));

        if (subscriptionRepository.existsBySubscriberAndSubscribedTo(subscriber, subscribedTo)) {
            throw new IllegalArgumentException("Ви вже підписані на цього користувача");
        }

        Subscription subscription = Subscription.builder()
                .subscriber(subscriber)
                .subscribedTo(subscribedTo)
                .createdAt(LocalDateTime.now())
                .build();

        subscriptionRepository.save(subscription);
        log.debug("Успішно створено підписку: {} -> {}", subscriberUsername, subscribedToUsername);
    }

    @Caching(evict = {
            @CacheEvict(value = "followers", key = "'subscribers_count:' + #subscribedToUsername"),
            @CacheEvict(value = "followers", key = "'subscriptions_count:' + #subscriberUsername"),
            @CacheEvict(value = "followers", key = "'is_following:' + #subscriberUsername + '_' + #subscribedToUsername"),
            @CacheEvict(value = "followers", key = "'following_users:' + #subscriberUsername"),
            @CacheEvict(value = "followers", key = "'followers_list:' + #subscribedToUsername")
    })
    public void unfollow(String subscriberUsername, String subscribedToUsername) {
        log.debug("Видалення підписки та очищення кешу: {} -> {}", subscriberUsername, subscribedToUsername);

        User subscriber = userRepository.findByUsername(subscriberUsername)
                .orElseThrow(() -> new UsernameNotFoundException("Користувача не знайдено: " + subscriberUsername));

        User subscribedTo = userRepository.findByUsername(subscribedToUsername)
                .orElseThrow(() -> new UsernameNotFoundException("Користувача не знайдено: " + subscribedToUsername));

        if (!subscriptionRepository.existsBySubscriberAndSubscribedTo(subscriber, subscribedTo)) {
            throw new IllegalArgumentException("Ви не підписані на цього користувача: " + subscribedToUsername);
        }

        subscriptionRepository.deleteBySubscriberAndSubscribedTo(subscriber, subscribedTo);
        log.debug("Успішно видалено підписку: {} -> {}", subscriberUsername, subscribedToUsername);
    }

    @Cacheable(
            value = "followers",
            key = "'is_following:' + #subscriberUsername + '_' + #subscribedToUsername",
            unless = "#result == false"
    )
    public boolean isFollowing(String subscriberUsername, String subscribedToUsername) {
        log.debug("Промах кэша: Проверка, подписан ли {} на {}", subscriberUsername, subscribedToUsername);

        if (subscriberUsername.equals(subscribedToUsername)) {
            return false;
        }

        User subscriber = userRepository.findByUsername(subscriberUsername)
                .orElseThrow(() -> new UsernameNotFoundException("Користувача не знайдено: " + subscriberUsername));

        User subscribedTo = userRepository.findByUsername(subscribedToUsername)
                .orElseThrow(() -> new UsernameNotFoundException("Користувача не знайдено: " + subscribedToUsername));

        boolean isFollowing = subscriptionRepository.existsBySubscriberAndSubscribedTo(subscriber, subscribedTo);
        log.debug("Результат проверки подписки: {} подписан на {}: {}", subscriberUsername, subscribedToUsername, isFollowing);
        return isFollowing;
    }

    @Cacheable(
            value = "followers",
            key = "'subscribers_count:' + #username",
            unless = "#result == 0"
    )
    public long getSubscribersCount(String username) {
        log.debug("Промах кешу: Розрахунок кількості підписників для: {}", username);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Користувача не знайдено: " + username));
        long count = subscriptionRepository.countBySubscribedTo(user);
        log.debug("Розраховано кількість підписників для {}: {}", username, count);
        return count;
    }

    @Cacheable(
            value = "followers",
            key = "'subscriptions_count:' + #username",
            unless = "#result == 0"
    )
    public long getSubscriptionsCount(String username) {
        log.debug("Промах кешу: Розрахунок кількості підписок для: {}", username);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Користувача не знайдено: " + username));
        long count = subscriptionRepository.countBySubscriber(user);
        log.debug("Розраховано кількість підписок для {}: {}", username, count);
        return count;
    }

    @Cacheable(
            value = "followers",
            key = "'following_users:' + #username",
            unless = "#result.isEmpty()"
    )
    public List<UserSearchDTO> getFollowingUsers(String username, String currentUsername) {
        log.debug("Промах кешу: Отримання підписок для: {}", username);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Користувача не знайдено: " + username));

        List<UserSearchDTO> results = subscriptionRepository.findBySubscriber(user)
                .stream()
                .map(subscription -> mapToUserSearchDTO(subscription.getSubscribedTo(), currentUsername))
                .collect(Collectors.toList());

        log.debug("Знайдено {} підписок для: {}", results.size(), username);
        return results;
    }

    @Cacheable(
            value = "followers",
            key = "'followers_list:' + #username",
            unless = "#result.isEmpty()"
    )
    public List<UserSearchDTO> getFollowers(String username, String currentUsername) {
        // Проверяем кеш перед выполнением метода
        Cache cache = cacheManager.getCache("followers");
        if (cache != null) {
            Cache.ValueWrapper cachedValue = cache.get("followers_list:" + username);
            if (cachedValue != null) {
                log.debug("✅ Данные о подписчиках для '{}' взяты из кеша", username);
            }
        }

        log.debug("⛔ Промах кеша: Отримання підписників для: {}", username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Користувача не знайдено: " + username));

        List<UserSearchDTO> results = subscriptionRepository.findBySubscribedTo(user)
                .stream()
                .map(subscription -> mapToUserSearchDTO(subscription.getSubscriber(), currentUsername))
                .collect(Collectors.toList());

        log.debug("🔹 Знайдено {} підписників для: {}", results.size(), username);
        return results;
    }

    private UserSearchDTO mapToUserSearchDTO(User user, String currentUsername) {
        return UserSearchDTO.builder()
                .username(user.getUsername())
                .avatar(user.getAvatar() != null ?
                        "data:image/jpeg;base64," + imageService.getBase64Avatar(user.getAvatar()) :
                        null)
                .followersCount(getSubscribersCount(user.getUsername()))
                .isFollowing(currentUsername != null &&
                        isFollowing(currentUsername, user.getUsername()))
                .build();
    }

    @Scheduled(fixedRate = 3600000) // Каждый час
    public void refreshCache() {
        log.info("Оновлення кешу підписок");
        cacheManager.getCache("followers").clear();
        log.info("Кеш підписок оновлено");
    }

}