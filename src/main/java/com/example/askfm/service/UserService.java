package com.example.askfm.service;

import com.example.askfm.dto.UserProfileDTO;
import com.example.askfm.dto.UserRegistrationDTO;
import com.example.askfm.dto.UserSearchDTO;
import com.example.askfm.model.User;
import com.example.askfm.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
                .password(passwordEncoder.encode(registrationDTO.getPassword()))
                .build();

        return userRepository.save(user);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }


    public List<UserSearchDTO> searchUsers(String query, String currentUsername) {
        if (query == null || query.trim().isEmpty()) {
            return Collections.emptyList();
        }

        return userRepository.searchByUsername(query.trim())
                .stream()
                .map(user -> UserSearchDTO.builder()
                        .username(user.getUsername())
                        .avatar(user.getAvatar())
                        .bio(user.getBio())
                        .followersCount(subscriptionService.getSubscribersCount(user.getUsername()))
                        .isFollowing(currentUsername != null &&
                                subscriptionService.isFollowing(currentUsername, user.getUsername()))
                        .build())
                .collect(Collectors.toList());
    }
}
