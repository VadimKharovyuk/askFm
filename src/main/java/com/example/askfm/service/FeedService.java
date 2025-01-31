package com.example.askfm.service;

import com.example.askfm.model.Post;
import com.example.askfm.model.User;
import com.example.askfm.repository.PostRepository;
import com.example.askfm.repository.SubscriptionRepository;
import com.example.askfm.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class FeedService {
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;

    public Page<Post> getFeedPosts(String username, Pageable pageable) {
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Користувача не знайдено: " + username));

        // Получаем ID всех пользователей, на которых подписан текущий пользователь
        List<Long> followingIds = subscriptionRepository.findBySubscriber(currentUser)
                .stream()
                .map(subscription -> subscription.getSubscribedTo().getId())
                .collect(Collectors.toList());

        // Если пользователь ни на кого не подписан, возвращаем пустую страницу
        if (followingIds.isEmpty()) {
            return Page.empty(pageable);
        }

        // Получаем посты от всех пользователей, на которых подписан текущий пользователь
        return postRepository.findByAuthorIdInOrderByPublishedAtDesc(followingIds, pageable);
    }
}
