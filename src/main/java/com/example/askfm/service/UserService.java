package com.example.askfm.service;

import com.example.askfm.config.CacheMonitor;
import com.example.askfm.dto.ChangePasswordDTO;
import com.example.askfm.dto.UserRegistrationDTO;
import com.example.askfm.dto.UserSearchDTO;
import com.example.askfm.enums.UserRole;
import com.example.askfm.exception.UserNotFoundException;
import com.example.askfm.model.User;
import com.example.askfm.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SubscriptionService subscriptionService ;
    private final ImageService imageService;
    private  final CacheManager cacheManager;



    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        // Проверяем пароль на null или пустоту
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            log.error("User {} has empty password", username);
            throw new IllegalStateException("User has no password set");
        }

        if (user.isLocked()) {
            throw new LockedException("Аккаунт заблокирован. " +
                    (user.getLockReason() != null ? "Причина: " + user.getLockReason() : ""));
        }

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .roles(user.getRole().name())
                .accountLocked(user.isLocked())
                .build();
    }

    public User registerNewUser(UserRegistrationDTO registrationDTO) {
        // Валидация входных данных
        if (registrationDTO.getPassword() == null || registrationDTO.getPassword().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }

        if (userRepository.existsByUsername(registrationDTO.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }

        if (userRepository.existsByEmail(registrationDTO.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        // Создание пользователя с обязательным паролем
        String encodedPassword = passwordEncoder.encode(registrationDTO.getPassword());
        if (encodedPassword == null || encodedPassword.isEmpty()) {
            throw new IllegalStateException("Failed to encode password");
        }

        User user = User.builder()
                .username(registrationDTO.getUsername())
                .email(registrationDTO.getEmail())
                .role(UserRole.USER)
                .createdAt(LocalDateTime.now())
                .password(encodedPassword)
                .balance(BigDecimal.ZERO)
                .locked(false)
                .build();
        return userRepository.save(user);
    }



    public List<UserSearchDTO> searchUsers(String query, String currentUsername) {
        Cache cache = cacheManager.getCache("userSearch");
        String cacheKey = "search:" + query.toLowerCase().trim();

        List<UserSearchDTO> cachedResults = cache != null ?
                (List<UserSearchDTO>) cache.get(cacheKey, List.class) : null;

        if (cachedResults != null) {
            log.debug("✅ Взято з кешу {} результатів для запиту: '{}'",
                    cachedResults.size(), query);
            return cachedResults;
        }

        if (!StringUtils.hasText(query)) {
            return Collections.emptyList();
        }

        long startTime = System.currentTimeMillis();
        String normalizedQuery = query.toLowerCase().trim();
        log.debug("⛔ Пошук в БД за запитом: '{}'", normalizedQuery);

        List<UserSearchDTO> results = userRepository.searchByUsername(normalizedQuery)
                .stream()
                .map(user -> mapToUserSearchDTO(user, currentUsername, cache))
                .collect(Collectors.toList());

        cache.put(cacheKey, results);
        long endTime = System.currentTimeMillis();
        log.debug("🔹 Знайдено в БД {} результатів за запитом: '{}' за {} мс",
                results.size(), normalizedQuery, endTime - startTime);
        return results;
    }

    private UserSearchDTO mapToUserSearchDTO(User user, String currentUsername, Cache cache) {
        String username = user.getUsername();

        Cache followersCache = cacheManager.getCache("followers");

        // Получаем количество подписчиков
        Long followersCount;
        String followersKey = "subscribers_count:" + username;
        Cache.ValueWrapper followersWrapper = followersCache.get(followersKey);
        if (followersWrapper != null) {
            followersCount = (Long) followersWrapper.get();
        } else {
            followersCount = subscriptionService.getSubscribersCount(username);
            followersCache.put(followersKey, followersCount);
        }

        // Проверяем подписку
        boolean isFollowing = false;
        if (currentUsername != null) {
            String followingKey = "is_following:" + currentUsername + "_" + username;
            Cache.ValueWrapper followingWrapper = followersCache.get(followingKey);
            if (followingWrapper != null) {
                isFollowing = (Boolean) followingWrapper.get();
            } else {
                isFollowing = subscriptionService.isFollowing(currentUsername, username);
                followersCache.put(followingKey, isFollowing);
            }
        }

        return UserSearchDTO.builder()
                .username(username)
                .avatar(user.getAvatar() != null ?
                        "data:image/jpeg;base64," + imageService.getBase64Avatar(user.getAvatar()) :
                        null)
                .followersCount(followersCount)
                .isFollowing(isFollowing)
                .build();
    }


    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
    }



    @Transactional
    public void updateUsername(String oldUsername, String newUsername) {
        if (userRepository.existsByUsername(newUsername)) {
            throw new IllegalArgumentException("Username already exists: " + newUsername);
        }

        User user = userRepository.findByUsername(oldUsername)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + oldUsername));

        // Сохраняем текущий контекст аутентификации
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        user.setUsername(newUsername);
        userRepository.save(user);

        // Создаем новое UserDetails с обновленным именем
        UserDetails updatedUser = org.springframework.security.core.userdetails.User
                .withUsername(newUsername)
                .password(user.getPassword())
                .authorities(auth.getAuthorities())
                .build();

        // Обновляем аутентификацию с сохранением прав
        Authentication newAuth = new UsernamePasswordAuthenticationToken(
                updatedUser,
                user.getPassword(),
                auth.getAuthorities()
        );

        SecurityContextHolder.getContext().setAuthentication(newAuth);
    }


    public User updateCover(String username, byte[] cover) {
        User user = findByUsername(username);
        user.setCover(cover);
        return userRepository.save(user);
    }

    public User updateAvatar(String username, byte[] avatar) {
        User user = findByUsername(username);
        user.setAvatar(avatar);
        return userRepository.save(user);
    }

    // Метод для повышения прав пользователя до админа
    public User promoteToAdmin(String username) {
        User user = findByUsername(username);
        user.setRole(UserRole.ADMIN);
        return userRepository.save(user);
    }

    // Метод для понижения прав
    public User demoteToUser(String username) {
        User user = findByUsername(username);
        user.setRole(UserRole.USER);
        return userRepository.save(user);
    }
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User updateUserRole(String username, UserRole newRole) {
        User user = findByUsername(username);

        // Проверка, чтобы последний админ не мог снять с себя права админа
        if (user.getRole() == UserRole.ADMIN && newRole != UserRole.ADMIN) {
            long adminCount = userRepository.countByRole(UserRole.ADMIN);
            if (adminCount <= 1) {
                throw new IllegalStateException("Cannot remove the last administrator");
            }
        }

        user.setRole(newRole);
        return userRepository.save(user);
    }

    public List<User> searchUsers(String query) {
        log.debug("Performing database search for users with query: '{}'", query);

        if (query == null || query.trim().isEmpty()) {
            log.debug("Search query is empty, returning empty list");
            return Collections.emptyList();
        }

        String trimmedQuery = query.trim();
        List<User> results = userRepository.findByUsernameContainingIgnoreCase(trimmedQuery);
        log.debug("Found {} users matching query: '{}'", results.size(), trimmedQuery);

        return results;
    }


    public long getTotalUsersCount() {
        return userRepository.count();
    }



    public double getNewRegistrationsGrowth() {
        // Процент роста новых регистраций за последний месяц
        LocalDateTime monthAgo = LocalDateTime.now().minusMonths(1);
        long previousCount = userRepository.countByCreatedAtBefore(monthAgo);
        long currentCount = userRepository.count();
        return previousCount > 0 ? ((currentCount - previousCount) * 100.0) / previousCount : 0;
    }

    public double getEngagementRate() {
        // Можно рассчитать на основе активности пользователей
        return 73.0; // Заглушка, реализуйте свою логику
    }

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
    }


    public void changePassword(String username, ChangePasswordDTO passwordDTO) {
        log.debug("Changing password for user: {}", username);

        // Проверка входных данных
        if (passwordDTO.getNewPassword() == null || passwordDTO.getNewPassword().isEmpty()) {
            throw new IllegalArgumentException("New password cannot be empty");
        }

        if (!passwordDTO.getNewPassword().equals(passwordDTO.getConfirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match");
        }

        User user = findByUsername(username);

        // Проверка старого пароля
        if (!passwordEncoder.matches(passwordDTO.getOldPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }

        // Кодирование и установка нового пароля
        String encodedPassword = passwordEncoder.encode(passwordDTO.getNewPassword());
        if (encodedPassword == null || encodedPassword.isEmpty()) {
            throw new IllegalStateException("Failed to encode new password");
        }

        user.setPassword(encodedPassword);
        userRepository.save(user);
        log.info("Password changed successfully for user: {}", username);
    }



    // Метод для блокировки пользователя
    public void lockUser(String username, String reason) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        user.setLocked(true);
        user.setLockedAt(LocalDateTime.now());
        user.setLockReason(reason);

        userRepository.save(user);
        log.info("User {} has been locked. Reason: {}", username, reason);
    }


    // Метод для разблокировки пользователя
    public void unlockUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        user.setLocked(false);
        user.setLockedAt(null);
        user.setLockReason(null);

        userRepository.save(user);
        log.info("User {} has been unlocked", username);
    }

    // Метод для проверки статуса блокировки
    public boolean isUserLocked(String username) {
        return userRepository.findByUsername(username)
                .map(User::isLocked)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    public User findByEmail(String email) {
       return userRepository.findByEmail(email);
    }

    public void save(User user) {
        userRepository.save(user);
    }
}