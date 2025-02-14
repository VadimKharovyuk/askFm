package com.example.askfm.service;

import com.example.askfm.dto.ProfileEditDTO;
import com.example.askfm.maper.UserProfileMapper;
import com.example.askfm.model.User;
import com.example.askfm.model.UserProfile;
import com.example.askfm.repository.UserProfileRepository;
import com.example.askfm.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class UserProfileService {
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final UserProfileMapper userProfileMapper;
    private final CacheManager cacheManager;

    public UserProfile getUserProfile(User user) {
        Cache profileCache = cacheManager.getCache("userProfiles");
        String cacheKey = "user_profile:" + user.getId();

        UserProfile userProfile = profileCache.get(cacheKey, UserProfile.class);
        if (userProfile != null) {
            log.debug("✅ Получен профиль пользователя {} из кеша", user.getUsername());
            return userProfile;
        }

        log.debug("⛔ Поиск профиля пользователя {} в БД", user.getUsername());
        userProfile = userProfileRepository.findByUser(user)
                .orElse(null);

        if (userProfile != null) {
            profileCache.put(cacheKey, userProfile);
            log.debug("🔹 Профиль пользователя {} сохранен в кеш", user.getUsername());
        }

        return userProfile;
    }

    public void updateProfile(String username, ProfileEditDTO profileDTO) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        UserProfile profile = user.getProfile();
        if (profile == null) {
            profile = new UserProfile();
            profile.setUser(user);
        }

        profile = userProfileMapper.toEntity(profileDTO, profile);
        userProfileRepository.save(profile);

        // Обновляем кеш после изменения профиля
        Cache profileCache = cacheManager.getCache("userProfiles");
        String cacheKey = "user_profile:" + user.getId();
        profileCache.put(cacheKey, profile);
        log.debug("🔄 Обновлен кеш профиля пользователя {}", username);
    }

    public ProfileEditDTO getProfileDTO(User user) {
        UserProfile profile = getUserProfile(user); // Используем кешированный метод
        return userProfileMapper.toDto(profile);
    }

    // Вспомогательный метод для очистки кеша профиля
    public void evictProfileCache(Long userId) {
        Cache profileCache = cacheManager.getCache("userProfiles");
        String cacheKey = "user_profile:" + userId;
        profileCache.evict(cacheKey);
        log.debug("❌ Очищен кеш профиля пользователя с ID {}", userId);
    }
}