package com.example.askfm.service;

import com.example.askfm.dto.SubscriptionDTO;
import com.example.askfm.dto.UpcomingBirthdayDTO;
import com.example.askfm.dto.UserSearchDTO;
import com.example.askfm.model.Subscription;
import com.example.askfm.model.User;
import com.example.askfm.repository.SubscriptionRepository;
import com.example.askfm.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class SubscriptionService {
    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    private final ImageService imageService;


    public void follow(String subscriberUsername, String subscribedToUsername) {
        // Проверяем, что пользователь не пытается подписаться на самого себя
        if (subscriberUsername.equals(subscribedToUsername)) {
            throw new IllegalArgumentException("Ви не можете підписатися на себе");
        }

        // Получаем пользователей
        User subscriber = userRepository.findByUsername(subscriberUsername)
                .orElseThrow(() -> new UsernameNotFoundException("Користувача не знайдено: " + subscriberUsername));

        User subscribedTo = userRepository.findByUsername(subscribedToUsername)
                .orElseThrow(() -> new UsernameNotFoundException("Користувача не знайдено: " + subscribedToUsername));

        // Проверяем, существует ли уже подписка
        if (subscriptionRepository.existsBySubscriberAndSubscribedTo(subscriber, subscribedTo)) {
            throw new IllegalArgumentException("Ви вже підписані на цього користувача");
        }

        // Создаем новую подписку
        Subscription subscription = Subscription.builder()
                .subscriber(subscriber)
                .subscribedTo(subscribedTo)
                .createdAt(LocalDateTime.now())
                .build();

        subscriptionRepository.save(subscription);
    }

    public void unfollow(String subscriberUsername, String subscribedToUsername) {
        User subscriber = userRepository.findByUsername(subscriberUsername)
                .orElseThrow(() -> new UsernameNotFoundException("Користувача не знайдено: " + subscriberUsername));

        User subscribedTo = userRepository.findByUsername(subscribedToUsername)
                .orElseThrow(() -> new UsernameNotFoundException("Користувача не знайдено: " + subscribedToUsername));

        subscriptionRepository.deleteBySubscriberAndSubscribedTo(subscriber, subscribedTo);
    }

//    public List<SubscriptionDTO> getSubscriptions(String username) {
//        User user = userRepository.findByUsername(username)
//                .orElseThrow(() -> new UsernameNotFoundException("Користувача не знайдено: " + username));
//
//        return subscriptionRepository.findBySubscriber(user)
//                .stream()
//                .map(this::convertToDTO)
//                .collect(Collectors.toList());
//    }

//    public List<SubscriptionDTO> getSubscribers(String username) {
//        User user = userRepository.findByUsername(username)
//                .orElseThrow(() -> new UsernameNotFoundException("Користувача не знайдено: " + username));
//
//        return subscriptionRepository.findBySubscribedTo(user)
//                .stream()
//                .map(this::convertToDTO)
//                .collect(Collectors.toList());
//    }

    public boolean isFollowing(String subscriberUsername, String subscribedToUsername) {
        User subscriber = userRepository.findByUsername(subscriberUsername)
                .orElseThrow(() -> new UsernameNotFoundException("Користувача не знайдено: " + subscriberUsername));

        User subscribedTo = userRepository.findByUsername(subscribedToUsername)
                .orElseThrow(() -> new UsernameNotFoundException("Користувача не знайдено: " + subscribedToUsername));

        return subscriptionRepository.existsBySubscriberAndSubscribedTo(subscriber, subscribedTo);
    }

    public long getSubscribersCount(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Користувача не знайдено: " + username));

        return subscriptionRepository.countBySubscribedTo(user);
    }

    public List<UserSearchDTO> getFollowingUsers(String username, String currentUsername) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Користувача не знайдено: " + username));

        return subscriptionRepository.findBySubscriber(user)
                .stream()
                .map(subscription -> UserSearchDTO.builder()
                        .username(subscription.getSubscribedTo().getUsername())
                        .avatar(subscription.getSubscribedTo().getAvatar() != null ?
                                "data:image/jpeg;base64," + imageService.getBase64Avatar(subscription.getSubscribedTo().getAvatar()) :
                                null)
                        .followersCount(getSubscribersCount(subscription.getSubscribedTo().getUsername()))
                        .isFollowing(currentUsername != null &&
                                isFollowing(currentUsername, subscription.getSubscribedTo().getUsername()))
                        .build())
                .collect(Collectors.toList());
    }


    public List<UserSearchDTO> getFollowers(String username, String currentUsername) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Користувача не знайдено: " + username));

        return subscriptionRepository.findBySubscribedTo(user)
                .stream()
                .map(subscription -> UserSearchDTO.builder()
                        .username(subscription.getSubscriber().getUsername())
                        .avatar(subscription.getSubscriber().getAvatar() != null ?
                                "data:image/jpeg;base64," + imageService.getBase64Avatar(subscription.getSubscriber().getAvatar()) :
                                null)
                        .followersCount(getSubscribersCount(subscription.getSubscriber().getUsername()))
                        .isFollowing(currentUsername != null &&
                                isFollowing(currentUsername, subscription.getSubscriber().getUsername()))
                        .build())
                .collect(Collectors.toList());
    }

    private SubscriptionDTO convertToDTO(Subscription subscription) {
        return SubscriptionDTO.builder()
                .subscriberUsername(subscription.getSubscriber().getUsername())
                .subscribedToUsername(subscription.getSubscribedTo().getUsername())
                .createdAt(subscription.getCreatedAt())
                .build();
    }
}