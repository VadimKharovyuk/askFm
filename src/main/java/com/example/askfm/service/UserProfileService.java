package com.example.askfm.service;

import com.example.askfm.dto.ProfileEditDTO;

import com.example.askfm.maper.UserProfileMapper;
import com.example.askfm.model.User;
import com.example.askfm.model.UserProfile;
import com.example.askfm.repository.UserProfileRepository;
import com.example.askfm.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class UserProfileService {
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final UserProfileMapper userProfileMapper;

    public UserProfile getUserProfile(User user) {
        return userProfileRepository.findByUser(user)
                .orElse(null);
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
    }

    public ProfileEditDTO getProfileDTO(User user) {
        UserProfile profile = user.getProfile();
        return userProfileMapper.toDto(profile);
    }
}