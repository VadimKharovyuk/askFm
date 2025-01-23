package com.example.askfm.service;

import com.example.askfm.dto.UserSuggestionDTO;
import com.example.askfm.model.User;
import com.example.askfm.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SuggestedUsersService {
    private final UserRepository userRepository;
    private final ImageService imageService;
    private final static int SUGGESTIONS_LIMIT = 3;

    public List<UserSuggestionDTO> getSuggestedUsers(String currentUsername) {
        // Получаем пользователей из базы данных
        List<User> suggestedUsers = userRepository.findSuggestedUsers(currentUsername, SUGGESTIONS_LIMIT);

        // Преобразуем в DTO
        return suggestedUsers.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private UserSuggestionDTO convertToDTO(User user) {
        return UserSuggestionDTO.builder()
                .username(user.getUsername())
                .avatarBase64(imageService.getBase64Avatar(user.getAvatar()))
                .subscribersCount(user.getSubscribers().size())
                .build();
    }
}

