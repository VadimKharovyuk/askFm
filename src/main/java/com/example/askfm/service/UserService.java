package com.example.askfm.service;

import com.example.askfm.config.CacheMonitor;
import com.example.askfm.dto.*;
import com.example.askfm.enums.UserRole;
import com.example.askfm.exception.UserNotFoundException;
import com.example.askfm.maper.UserMapper;
import com.example.askfm.model.BlockInfo;
import com.example.askfm.model.User;
import com.example.askfm.repository.BlockInfoRepository;
import com.example.askfm.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
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
    private final UserMapper userMapper;
private final BlockInfoRepository blockInfoRepository;


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
        // Проверяем наличие кеша
        Cache cache = cacheManager.getCache("userSearch");
        String cacheKey = "search:" + (query != null ? query.toLowerCase().trim() : "") +
                ":user:" + currentUsername;

        // Пытаемся получить результаты из кеша
        List<UserSearchDTO> cachedResults = cache != null ?
                (List<UserSearchDTO>) cache.get(cacheKey, List.class) : null;

        if (cachedResults != null) {
            log.debug("✅ Получено из кеша {} результатов для запроса: '{}'",
                    cachedResults.size(), query);
            return cachedResults;
        }

        // Если запрос пустой, возвращаем пустой список
        if (!StringUtils.hasText(query)) {
            return Collections.emptyList();
        }

        // Если в кеше нет результатов, выполняем поиск
        long startTime = System.currentTimeMillis();
        String normalizedQuery = query.toLowerCase().trim();
        log.debug("⛔ Поиск в БД по запросу: '{}'", normalizedQuery);

        // Получаем результаты из базы данных
        List<UserSearchDTO> results = userRepository.searchByUsername(normalizedQuery)
                .stream()
                .map(user -> userMapper.toSearchDTO(user, currentUsername))
                .collect(Collectors.toList());

        // Сохраняем результаты в кеш
        if (cache != null) {
            cache.put(cacheKey, results);
            log.debug("💾 Результаты сохранены в кеш для запроса: '{}'", normalizedQuery);
        }

        // Логируем время выполнения
        long endTime = System.currentTimeMillis();
        log.debug("🔹 Найдено в БД {} результатов по запросу: '{}' за {} мс",
                results.size(), normalizedQuery, endTime - startTime);

        return results;
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


//заблокировать
    @Transactional
    public BlockInfoDTO blockUser(String blockerUsername, String blockedUsername) {
        // Проверяем входные данные
        if (blockerUsername == null || blockedUsername == null) {
            throw new IllegalArgumentException("Username cannot be null");
        }

        User blocker = findByUsername(blockerUsername);
        User blocked = findByUsername(blockedUsername);

        // Проверяем, не пытается ли пользователь заблокировать сам себя
        if (blocker.getId().equals(blocked.getId())) {
            throw new IllegalArgumentException("Нельзя заблокировать самого себя");
        }

        // Проверяем, не заблокирован ли уже пользователь
        if (isUserBlocked(blockerUsername, blockedUsername)) {
            throw new IllegalStateException("Пользователь уже заблокирован");
        }

        // Добавляем в множество заблокированных
        blocker.getBlockedUsers().add(blocked);
        userRepository.save(blocker); // Явно сохраняем изменения

        // Создаем запись о блокировке
        BlockInfo blockInfo = userMapper.toBlockInfo(blocker, blocked);
        blockInfoRepository.save(blockInfo);

        log.info("User {} blocked user {}", blockerUsername, blockedUsername);
        return userMapper.toBlockInfoDTO(blockInfo);
    }
//разблокировать
    @Transactional
    public void unblockUser(String blockerUsername, String blockedUsername) {
        if (blockerUsername == null || blockedUsername == null) {
            throw new IllegalArgumentException("Username cannot be null");
        }

        User blocker = findByUsername(blockerUsername);
        User blocked = findByUsername(blockedUsername);

        // Проверяем, заблокирован ли пользователь
        if (!isUserBlocked(blockerUsername, blockedUsername)) {
            throw new IllegalStateException("Пользователь не был заблокирован");
        }

        // Удаляем из множества заблокированных
        blocker.getBlockedUsers().remove(blocked);
        userRepository.save(blocker); // Явно сохраняем изменения

        // Удаляем запись о блокировке
        blockInfoRepository.deleteByBlockerAndBlocked(blocker, blocked);

        log.info("User {} unblocked user {}", blockerUsername, blockedUsername);
    }


//черный список
@Transactional(readOnly = true)
public Page<BlockedUserDTO> getBlockedUsers(String username) {
    if (username == null) {
        throw new IllegalArgumentException("Username cannot be null");
    }

    User user = findByUsername(username);
    // Создаем Pageable для первых 20 записей, сортированных по дате блокировки
    Pageable pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "blockedAt"));

    // Получаем страницу с блокировками
    Page<BlockInfo> blockInfoPage = blockInfoRepository.findByBlocker(user, pageable);

    // Преобразуем в DTO
    return blockInfoPage.map(blockInfo -> userMapper.toBlockedUserDTO(blockInfo.getBlocked(), blockInfo));
}

    @Transactional(readOnly = true)
    public boolean isUserBlocked(String blockerUsername, String blockedUsername) {
        if (blockerUsername == null || blockedUsername == null) {
            return false;
        }

        try {
            User blocker = findByUsername(blockerUsername);
            User blocked = findByUsername(blockedUsername);
            return blockInfoRepository.existsByBlockerAndBlocked(blocker, blocked);
        } catch (UsernameNotFoundException e) {
            return false;
        }
    }

    // Вспомогательный метод для получения количества заблокированных пользователей
    @Transactional(readOnly = true)
    public long getBlockedUsersCount(String username) {
        if (username == null) {
            throw new IllegalArgumentException("Username cannot be null");
        }

        User user = findByUsername(username);
        return blockInfoRepository.countByBlocker(user);
    }

    private final SecureRandom random = new SecureRandom();
    private static final int MIN_REWARD = 1;
    private static final int MAX_REWARD = 5;

    public Optional<BigDecimal> claimDailyReward(String username) {
        return userRepository.findByUsername(username)
                .map(user -> {
                    if (user.getLastRewardDate() != null && user.getLastRewardDate().isEqual(LocalDate.now())) {
                        return Optional.<BigDecimal>empty(); // Уже получал награду
                    }

                    // Генерируем случайную награду
                    int reward = random.nextInt(MAX_REWARD - MIN_REWARD + 1) + MIN_REWARD;
                    BigDecimal rewardAmount = BigDecimal.valueOf(reward);

                    // Обновляем баланс и дату последней награды
                    user.setBalance(user.getBalance().add(rewardAmount));
                    user.setLastRewardDate(LocalDate.now());
                    userRepository.save(user);

                    return Optional.of(rewardAmount);
                })
                .orElse(Optional.empty()); // Если пользователя нет, просто ничего не возвращаем
    }


    public boolean canClaimDailyReward(String username) {
        return userRepository.findByUsername(username)
                .map(user -> user.getLastRewardDate() == null || !user.getLastRewardDate().isEqual(LocalDate.now()))
                .orElse(false);
    }

    public Optional<BigDecimal> claimPageVisitReward(String username) {
        final BigDecimal PAGE_VISIT_REWARD = BigDecimal.ONE; // 1 бонус за посещение

        return userRepository.findByUsername(username)
                .map(user -> {
                    // Проверяем, получал ли пользователь награду сегодня
                    if (user.getLastPageVisitDate() != null &&
                            user.getLastPageVisitDate().isEqual(LocalDate.now())) {
                        return Optional.<BigDecimal>empty(); // Уже получал награду сегодня
                    }

                    // Обновляем баланс и дату последнего посещения
                    user.setBalance(user.getBalance().add(PAGE_VISIT_REWARD));
                    user.setLastPageVisitDate(LocalDate.now());
                    userRepository.save(user);

                    return Optional.of(PAGE_VISIT_REWARD);
                })
                .orElse(Optional.empty());
    }

    // Метод для проверки возможности получения награды
    public boolean canClaimPageVisitReward(String username) {
        return userRepository.findByUsername(username)
                .map(user -> user.getLastPageVisitDate() == null ||
                        !user.getLastPageVisitDate().isEqual(LocalDate.now()))
                .orElse(false);
    }
}