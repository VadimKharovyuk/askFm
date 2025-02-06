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
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class SubscriptionService {
    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    private final ImageService imageService;
    private final CacheManager cacheManager;

    public void follow(String subscriberUsername, String subscribedToUsername) {
        log.debug("⛔ Створення підписки: {} -> {}", subscriberUsername, subscribedToUsername);

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

        // Очистка кэша
        Cache cache = cacheManager.getCache("followers");
        cache.evict("subscribers_count:" + subscribedToUsername);
        cache.evict("subscriptions_count:" + subscriberUsername);
        cache.evict("is_following:" + subscriberUsername + "_" + subscribedToUsername);
        cache.evict("following_users:" + subscriberUsername);
        cache.evict("followers_list:" + subscribedToUsername);

        log.debug("✅ Успішно створено підписку: {} -> {}", subscriberUsername, subscribedToUsername);
    }

    public void unfollow(String subscriberUsername, String subscribedToUsername) {
        log.debug("⛔ Видалення підписки: {} -> {}", subscriberUsername, subscribedToUsername);

        User subscriber = userRepository.findByUsername(subscriberUsername)
                .orElseThrow(() -> new UsernameNotFoundException("Користувача не знайдено: " + subscriberUsername));

        User subscribedTo = userRepository.findByUsername(subscribedToUsername)
                .orElseThrow(() -> new UsernameNotFoundException("Користувача не знайдено: " + subscribedToUsername));

        if (!subscriptionRepository.existsBySubscriberAndSubscribedTo(subscriber, subscribedTo)) {
            throw new IllegalArgumentException("Ви не підписані на цього користувача: " + subscribedToUsername);
        }

        subscriptionRepository.deleteBySubscriberAndSubscribedTo(subscriber, subscribedTo);

        // Очистка кэша
        Cache cache = cacheManager.getCache("followers");
        cache.evict("subscribers_count:" + subscribedToUsername);
        cache.evict("subscriptions_count:" + subscriberUsername);
        cache.evict("is_following:" + subscriberUsername + "_" + subscribedToUsername);
        cache.evict("following_users:" + subscriberUsername);
        cache.evict("followers_list:" + subscribedToUsername);

        log.debug("✅ Успішно видалено підписку: {} -> {}", subscriberUsername, subscribedToUsername);
    }

//    @Caching(evict = {
//            @CacheEvict(value = "followers", key = "'subscribers_count:' + #subscribedToUsername"),
//            @CacheEvict(value = "followers", key = "'subscriptions_count:' + #subscriberUsername"),
//            @CacheEvict(value = "followers", key = "'is_following:' + #subscriberUsername + '_' + #subscribedToUsername"),
//            @CacheEvict(value = "followers", key = "'following_users:' + #subscriberUsername"),
//            @CacheEvict(value = "followers", key = "'followers_list:' + #subscribedToUsername")
//    })
//    public void follow(String subscriberUsername, String subscribedToUsername) {
//        log.debug("Створення підписки та очищення кешу: {} -> {}", subscriberUsername, subscribedToUsername);
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
//        log.debug("Успішно створено підписку: {} -> {}", subscriberUsername, subscribedToUsername);
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
//        log.debug("Видалення підписки та очищення кешу: {} -> {}", subscriberUsername, subscribedToUsername);
//
//        User subscriber = userRepository.findByUsername(subscriberUsername)
//                .orElseThrow(() -> new UsernameNotFoundException("Користувача не знайдено: " + subscriberUsername));
//
//        User subscribedTo = userRepository.findByUsername(subscribedToUsername)
//                .orElseThrow(() -> new UsernameNotFoundException("Користувача не знайдено: " + subscribedToUsername));
//
//        if (!subscriptionRepository.existsBySubscriberAndSubscribedTo(subscriber, subscribedTo)) {
//            throw new IllegalArgumentException("Ви не підписані на цього користувача: " + subscribedToUsername);
//        }
//
//        subscriptionRepository.deleteBySubscriberAndSubscribedTo(subscriber, subscribedTo);
//        log.debug("Успішно видалено підписку: {} -> {}", subscriberUsername, subscribedToUsername);
//    }


    public boolean isFollowing(String subscriberUsername, String subscribedToUsername) {
        Cache cache = cacheManager.getCache("followers");
        String cacheKey = "is_following:" + subscriberUsername + "_" + subscribedToUsername;

        Boolean cachedResult = cache != null ?
                cache.get(cacheKey, Boolean.class) : null;

        if (cachedResult != null) {
            log.debug("✅ Взято з кешу: {} підписан на {}: {}",
                    subscriberUsername, subscribedToUsername, cachedResult);
            return cachedResult;
        }

        if (subscriberUsername.equals(subscribedToUsername)) {
            return false;
        }

        log.debug("⛔ Пошук в БД: Перевірка підписки {} на {}",
                subscriberUsername, subscribedToUsername);

        User subscriber = userRepository.findByUsername(subscriberUsername)
                .orElseThrow(() -> new UsernameNotFoundException("Користувача не знайдено: " + subscriberUsername));

        User subscribedTo = userRepository.findByUsername(subscribedToUsername)
                .orElseThrow(() -> new UsernameNotFoundException("Користувача не знайдено: " + subscribedToUsername));

        boolean isFollowing = subscriptionRepository.existsBySubscriberAndSubscribedTo(subscriber, subscribedTo);

        cache.put(cacheKey, isFollowing);
        log.debug("🔹 Знайдено в БД: {} підписан на {}: {}",
                subscriberUsername, subscribedToUsername, isFollowing);
        return isFollowing;
    }



    public long getSubscribersCount(String username) {
        Cache cache = cacheManager.getCache("followers");
        String cacheKey = "subscribers_count:" + username;

        Long cachedCount = cache != null ?
                cache.get(cacheKey, Long.class) : null;

        if (cachedCount != null) {
            log.debug("✅ Взято з кешу кількість підписників для: {}: {}", username, cachedCount);
            return cachedCount;
        }

        log.debug("⛔ Пошук в БД: Розрахунок кількості підписників для: {}", username);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Користувача не знайдено: " + username));
        long count = subscriptionRepository.countBySubscribedTo(user);

        cache.put(cacheKey, count);
        log.debug("🔹 Знайдено в БД кількість підписників для {}: {}", username, count);
        return count;
    }


public long getSubscriptionsCount(String username) {
    Cache cache = cacheManager.getCache("followers");
    String cacheKey = "subscriptions_count:" + username;

    Long cachedCount = cache != null ?
            cache.get(cacheKey, Long.class) : null;

    if (cachedCount != null) {
        log.debug("✅ Взято з кешу кількість підписок для: {}: {}", username, cachedCount);
        return cachedCount;
    }

    User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("Користувача не знайдено: " + username));
    long count = subscriptionRepository.countBySubscriber(user);

    cache.put(cacheKey, count);
    log.debug("🔹 Знайдено в БД кількість підписок для {}: {}", username, count);
    return count;
}



    public List<UserSearchDTO> getFollowingUsers(String username, String currentUsername) {
        Cache cache = cacheManager.getCache("followers");
        String cacheKey = "following_users:" + username;

        List<UserSearchDTO> cachedResults = cache != null ?
                (List<UserSearchDTO>) cache.get(cacheKey, List.class) : null;

        if (cachedResults != null) {
            log.debug("✅ Взято з кешу {} підписок для: {}", cachedResults.size(), username);
            return cachedResults;
        }

        log.debug("⛔ Пошук в БД: Отримання підписок для: {}", username);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Користувача не знайдено: " + username));

        List<UserSearchDTO> results = subscriptionRepository.findBySubscriber(user)
                .stream()
                .map(subscription -> mapToUserSearchDTO(subscription.getSubscribedTo(), currentUsername))
                .collect(Collectors.toList());

        cache.put(cacheKey, results);
        log.debug("🔹 Знайдено в БД {} підписок для: {}", results.size(), username);
        return results;
    }


    public List<UserSearchDTO> getFollowers(String username, String currentUsername) {
        Cache cache = cacheManager.getCache("followers");
        String cacheKey = "followers_list:" + username;

        List<UserSearchDTO> cachedResults = cache != null ?
                (List<UserSearchDTO>) cache.get(cacheKey, List.class) : null;

        if (cachedResults != null) {
            log.debug("✅ Взято з кешу {} підписників для: {}",
                    cachedResults.size(), username);
            return cachedResults;
        }

        log.debug("⛔ Пошук в БД: Отримання підписників для: {}", username);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Користувача не знайдено: " + username));

        List<UserSearchDTO> results = subscriptionRepository.findBySubscribedTo(user)
                .stream()
                .map(subscription -> mapToUserSearchDTO(subscription.getSubscriber(), currentUsername))
                .collect(Collectors.toList());

        cache.put(cacheKey, results);
        log.debug("🔹 Знайдено в БД {} підписників для: {}", results.size(), username);
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


}