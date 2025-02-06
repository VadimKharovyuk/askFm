package com.example.askfm.service;

import com.example.askfm.dto.UserSuggestionDTO;
import com.example.askfm.model.User;
import com.example.askfm.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
@Slf4j

@Service
@RequiredArgsConstructor
public class SuggestedUsersService {
    private final UserRepository userRepository;
    private final ImageService imageService;
    private final static int SUGGESTIONS_LIMIT = 3;
    private final CacheManager cacheManager;

    public List<UserSuggestionDTO> getSuggestedUsers(String currentUsername) {
        Cache cache = cacheManager.getCache("suggestions");
        String cacheKey = "suggestions:" + currentUsername;

        List<UserSuggestionDTO> cachedResults = cache != null ?
                (List<UserSuggestionDTO>) cache.get(cacheKey, List.class) : null;

        if (cachedResults != null) {
            log.debug("✅ Взято з кешу рекомендації для: {}, кількість: {}",
                    currentUsername, cachedResults.size());
            return cachedResults;
        }

        log.debug("⛔ Пошук в БД: Отримання рекомендацій для: {}", currentUsername);
        List<User> suggestedUsers = userRepository.findSuggestedUsers(currentUsername, SUGGESTIONS_LIMIT);

        List<UserSuggestionDTO> results = suggestedUsers.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        cache.put(cacheKey, results);
        log.debug("🔹 Знайдено в БД {} рекомендацій для: {}", results.size(), currentUsername);
        return results;
    }
    // Новый метод с пагинацией
    public Page<UserSuggestionDTO> getPaginatedSuggestedUsers(String currentUsername, Pageable pageable) {
        Page<User> users = userRepository.findPaginatedSuggestedUsers(currentUsername, pageable);
        return users.map(this::convertToDTO);
    }



    private UserSuggestionDTO convertToDTO(User user) {
        return UserSuggestionDTO.builder()
                .username(user.getUsername())
                .avatarBase64(imageService.getBase64Avatar(user.getAvatar()))
                .subscribersCount(user.getSubscribers().size())
                .build();
    }
}

