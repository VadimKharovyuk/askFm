package com.example.askfm.service;

import com.example.askfm.dto.*;
import com.example.askfm.enums.ReactionType;
import com.example.askfm.exception.StoryNotFoundException;
import com.example.askfm.exception.UserNotFoundException;
import com.example.askfm.maper.StoryMapper;
import com.example.askfm.model.Story;
import com.example.askfm.model.StoryReaction;
import com.example.askfm.model.StoryView;
import com.example.askfm.model.User;
import com.example.askfm.repository.StoryReactionRepository;
import com.example.askfm.repository.StoryRepository;
import com.example.askfm.repository.StoryViewRepository;
import com.example.askfm.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class StoryService {
    private final StoryRepository storyRepository;
    private final UserRepository userRepository;
    private final StoryViewRepository viewRepository;
    private final StoryReactionRepository reactionRepository;
    private final StoryMapper storyMapper;
    private final ImgurStorageService imgurStorageService;
    private final SubscriptionService subscriptionService;

    private static final int PAGE_SIZE = 10;

    public StoryResponseDto createStory(CreateStoryDto dto) {
        User user = userRepository.findByUsername(dto.getUsername())
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        ImgurStorageService.ImgurUploadResult imgurResult = imgurStorageService.saveImage(dto.getImageData());
        Story story = Story.builder()
                .user(user)
                .imageUrl(imgurResult.getImageUrl())
                .deleteHash(imgurResult.getDeleteHash())
                .createdAt(LocalDateTime.now())
                .build();

        Story savedStory = storyRepository.save(story);
        return storyMapper.toResponseDto(savedStory);
    }

    // Добавляем метод для удаления истории
    public void deleteStory(Long storyId) {
        Story story = storyRepository.findById(storyId)
                .orElseThrow(() -> new StoryNotFoundException("Story not found"));

        // Удаляем изображение из Imgur
        if (story.getDeleteHash() != null) {
            imgurStorageService.deleteImage(story.getDeleteHash());
        }
        // Удаляем историю из базы данных
        storyRepository.delete(story);
    }

    public Page<StoryResponseDto> getActiveStories(int page) {
        LocalDateTime oneDayAgo = LocalDateTime.now().minusHours(24);
        Page<Story> storiesPage = storyRepository.findByCreatedAtAfterOrderByCreatedAtDesc(
                oneDayAgo,
                PageRequest.of(page, PAGE_SIZE)
        );
        return storiesPage.map(storyMapper::toResponseDto);
    }

    public Page<StoryResponseDto> getSubscribedUserStories(String username, int page) {
        LocalDateTime oneDayAgo = LocalDateTime.now().minusHours(24);
        List<UserSearchDTO> followingUsers = subscriptionService.getFollowingUsers(username, username);

        List<String> followingUsernames = followingUsers.stream()
                .map(UserSearchDTO::getUsername)
                .collect(Collectors.toList());

        if (followingUsernames.isEmpty()) {
            return Page.empty();
        }

        Page<Story> storiesPage = storyRepository
                .findByUserUsernameInAndCreatedAtAfterOrderByCreatedAtDesc(
                        followingUsernames,
                        oneDayAgo,
                        PageRequest.of(page, PAGE_SIZE)
                );

        return storiesPage.map(storyMapper::toResponseDto);
    }

    public Page<StoryResponseDto> getUserActiveStories(String username, int page) {
        LocalDateTime oneDayAgo = LocalDateTime.now().minusHours(24);
        Page<Story> storiesPage = storyRepository.findActiveStoriesByUsername(
                username,
                oneDayAgo,
                PageRequest.of(page, PAGE_SIZE)
        );

        return storiesPage.map(storyMapper::toResponseDto);
    }


    public List<StoryResponseDto> getUserActiveStories(String username) {
        LocalDateTime oneDayAgo = LocalDateTime.now().minusHours(24);
        List<Story> stories = storyRepository.findActiveStoriesByUsername(username, oneDayAgo);
        return stories.stream()
                .map(storyMapper::toResponseDto)
                .collect(Collectors.toList());
    }


    public List<StoryViewDto> getStoryViews(Long storyId) {
        Story story = storyRepository.findById(storyId)
                .orElseThrow(() -> new StoryNotFoundException("Story not found"));
        List<StoryView> views = viewRepository.findByStory(story);
        return storyMapper.toViewDtoList(views);
    }

    public List<StoryReactionDto> getStoryReactions(Long storyId) {
        Story story = storyRepository.findById(storyId)
                .orElseThrow(() -> new StoryNotFoundException("Story not found"));
        List<StoryReaction> reactions = reactionRepository.findByStory(story);
        return storyMapper.toReactionDtoList(reactions);
    }



    @Transactional(readOnly = true)
    public boolean isOwner(Long storyId, String username) {
        return storyRepository.findById(storyId)
                .map(story -> story.getUser().getUsername().equals(username))
                .orElseThrow(() -> new StoryNotFoundException("Story not found with id: " + storyId));
    }



    public UserProfileDto getUserProfile(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + username));

        List<Story> userStories = storyRepository.findByUserUsernameOrderByCreatedAtDesc(username);
        return storyMapper.toUserProfileDto(user, userStories);
    }


    public void addView(Long storyId, Long userId) {
        Story story = storyRepository.findById(storyId)
                .orElseThrow(() -> new StoryNotFoundException("Story not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        // Проверяем, не смотрел ли уже пользователь эту историю
        boolean hasViewed = viewRepository.existsByStoryAndUser(story, user);
        if (!hasViewed) {
            StoryView view = StoryView.builder()
                    .story(story)
                    .user(user)
                    .viewedAt(LocalDateTime.now())
                    .build();
            viewRepository.save(view);
        }
    }

    public void addReaction(Long storyId, Long userId, ReactionType reactionType) {
        Story story = storyRepository.findById(storyId)
                .orElseThrow(() -> new StoryNotFoundException("Story not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        // Проверяем, не реагировал ли уже пользователь
        Optional<StoryReaction> existingReaction =
                reactionRepository.findByStoryAndUser(story, user);

        if (existingReaction.isPresent()) {
            // Обновляем существующую реакцию
            StoryReaction reaction = existingReaction.get();
            reaction.setReactionType(reactionType);
            reaction.setReactedAt(LocalDateTime.now());
            reactionRepository.save(reaction);
        } else {
            // Создаем новую реакцию
            StoryReaction reaction = StoryReaction.builder()
                    .story(story)
                    .user(user)
                    .reactionType(reactionType)
                    .reactedAt(LocalDateTime.now())
                    .build();
            reactionRepository.save(reaction);
        }
    }


    /**
     * Удаляет истории старше 24 часов
     */
    @Transactional
    public void deleteExpiredStories() {
        LocalDateTime oneDayAgo = LocalDateTime.now().minusHours(24);

        List<Story> expiredStories = storyRepository.findByCreatedAtBefore(oneDayAgo);

        log.info("Found {} expired stories to delete", expiredStories.size());

        for (Story story : expiredStories) {
            try {
                // Удаляем изображение из Imgur
                if (story.getDeleteHash() != null) {
                    imgurStorageService.deleteImage(story.getDeleteHash());
                }

                // Удаляем историю из базы данных
                storyRepository.delete(story);

                log.debug("Deleted expired story: id={}, user={}, createdAt={}",
                        story.getId(), story.getUser().getUsername(), story.getCreatedAt());
            } catch (Exception e) {
                log.error("Error deleting expired story {}: {}", story.getId(), e.getMessage());
            }
        }

        log.info("Successfully deleted {} expired stories", expiredStories.size());
    }


//    /**
//     * Получить все истории пользователя
//     * @param username имя пользователя
//     * @return список историй
//     */
//    public List<StoryResponseDto> getUserStories(String username) {
//        List<Story> stories = storyRepository.findByUserUsernameOrderByCreatedAtDesc(username);
//        return stories.stream()
//                .map(storyMapper::toResponseDto)
//                .collect(Collectors.toList());
//    }

    //    public List<StoryResponseDto> getActiveStories() {
//        // Получаем истории за последние 24 часа
//        LocalDateTime oneDayAgo = LocalDateTime.now().minusHours(24);
//        List<Story> stories = storyRepository.findByCreatedAtAfter(oneDayAgo);
//        return stories.stream()
//                .map(storyMapper::toResponseDto)
//                .collect(Collectors.toList());
//    }


//
//    // Новый метод для историй от подписок
//    public List<StoryResponseDto> getSubscribedUserStories(String username) {
//        LocalDateTime oneDayAgo = LocalDateTime.now().minusHours(24);
//
//        // Получаем список пользователей, на которых подписан текущий пользователь
//        List<UserSearchDTO> followingUsers = subscriptionService.getFollowingUsers(username, username);
//
//        // Получаем их username'ы
//        List<String> followingUsernames = followingUsers.stream()
//                .map(UserSearchDTO::getUsername)
//                .collect(Collectors.toList());
//
//        // Если список пуст, возвращаем пустой список
//        if (followingUsernames.isEmpty()) {
//            return Collections.emptyList();
//        }
//
//        // Получаем истории только от этих пользователей
//        List<Story> stories = storyRepository.findByUserUsernameInAndCreatedAtAfterOrderByCreatedAtDesc(
//                followingUsernames,
//                oneDayAgo
//        );
//
//        return stories.stream()
//                .map(storyMapper::toResponseDto)
//                .collect(Collectors.toList());
//    }
}
