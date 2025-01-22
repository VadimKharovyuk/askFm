package com.example.askfm.service;

import com.example.askfm.dto.UserRegistrationDTO;
import com.example.askfm.dto.UserSearchDTO;
import com.example.askfm.enums.UserRole;
import com.example.askfm.exception.UserNotFoundException;
import com.example.askfm.model.User;
import com.example.askfm.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SubscriptionService subscriptionService ;
    private final ImageService imageService;


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .roles("USER")
                .build();
    }

    public User registerNewUser(UserRegistrationDTO registrationDTO) {
        if (userRepository.existsByUsername(registrationDTO.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (userRepository.existsByEmail(registrationDTO.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        User user = User.builder()
                .username(registrationDTO.getUsername())
                .email(registrationDTO.getEmail())
                .role(UserRole.USER)
                .createdAt(LocalDateTime.now())
                .password(passwordEncoder.encode(registrationDTO.getPassword()))
                .build();;

        return userRepository.save(user);
    }

    public List<UserSearchDTO> searchUsers(String query, String currentUsername) {
        if (query == null || query.trim().isEmpty()) {
            return Collections.emptyList();
        }

        return userRepository.searchByUsername(query.trim())
                .stream()
                .map(user -> UserSearchDTO.builder()
                        .username(user.getUsername())
                        .avatar(user.getAvatar() != null ?
                                "data:image/jpeg;base64," + imageService.getBase64Avatar(user.getAvatar()) :
                                null)
                        .bio(user.getBio())
                        .followersCount(subscriptionService.getSubscribersCount(user.getUsername()))
                        .isFollowing(currentUsername != null &&
                                subscriptionService.isFollowing(currentUsername, user.getUsername()))
                        .build())
                .collect(Collectors.toList());
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
    }
    public User updateCover(String username, byte[] cover) {
        User user = findByUsername(username);
        user.setCover(cover);
        return userRepository.save(user);
    }

    // Специальный метод для обновления аватара
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
        return userRepository.findByUsernameContainingIgnoreCase(query);
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
}