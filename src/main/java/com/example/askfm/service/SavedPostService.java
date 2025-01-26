package com.example.askfm.service;

import com.example.askfm.dto.SavedPostDTO;
import com.example.askfm.exception.PostNotFoundException;
import com.example.askfm.model.Post;
import com.example.askfm.model.SavedPost;
import com.example.askfm.model.User;
import com.example.askfm.repository.PostRepository;
import com.example.askfm.repository.SavedPostRepository;
import com.example.askfm.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

// Service
@Service
@RequiredArgsConstructor
public class SavedPostService {
    private final SavedPostRepository savedPostRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public SavedPostDTO savePost(Long postId, String username) {
        if (savedPostRepository.existsByPostIdAndUserUsername(postId, username)) {
            return null; // Пост уже сохранен
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(username));

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException(postId));

        SavedPost savedPost = SavedPost.builder()
                .user(user)
                .post(post)
                .savedAt(LocalDateTime.now())
                .build();

        return convertToDTO(savedPostRepository.save(savedPost));
    }

    @Transactional
    public void removeSavedPost(Long postId, String username) {
        savedPostRepository.deleteByPostIdAndUserUsername(postId, username);
    }

    public List<SavedPostDTO> getUserSavedPosts(String username) {
        return savedPostRepository.findByUserUsernameOrderBySavedAtDesc(username)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private SavedPostDTO convertToDTO(SavedPost savedPost) {
        return SavedPostDTO.builder()
                .id(savedPost.getId())
                .postId(savedPost.getPost().getId())
                .authorUsername(savedPost.getPost().getAuthor().getUsername())
                .content(savedPost.getPost().getContent())
                .savedAt(savedPost.getSavedAt())
                .postPublishedAt(savedPost.getPost().getPublishedAt())
                .build();
    }
}
