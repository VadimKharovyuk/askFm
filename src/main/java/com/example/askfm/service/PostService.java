package com.example.askfm.service;

import com.example.askfm.dto.PostCreateDTO;
import com.example.askfm.dto.PostDTO;
import com.example.askfm.model.Post;
import com.example.askfm.model.User;
import com.example.askfm.repository.PostRepository;

import com.example.askfm.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final ImageService imageService;



    public Post createPost(String username, PostCreateDTO postDTO) {
        User author = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        byte[] mediaBytes = null;
        if (postDTO.getMedia() != null && !postDTO.getMedia().isEmpty()) {
            try {
                mediaBytes = imageService.resizeImage(postDTO.getMedia().getBytes(), 800);
            } catch (IOException e) {
                throw new RuntimeException("Failed to process image", e);
            }
        }
        Post post = Post.builder()
                .author(author)
                .content(postDTO.getContent())
                .media(mediaBytes)
                .publishedAt(LocalDateTime.now())
                .views(0L)

                .build();

        return postRepository.save(post);
    }



    public PostDTO getPostDTO(Post post, String currentUsername) {
        return PostDTO.builder()
                .id(post.getId())
                .authorUsername(post.getAuthor().getUsername())
                .authorAvatar(imageService.getBase64Avatar(post.getAuthor().getAvatar()))
                .content(post.getContent())
                .base64Media(post.getMedia() != null ?
                        imageService.getBase64Avatar(post.getMedia()) : null)
                .publishedAt(post.getPublishedAt())
                .views(post.getViews())
                .likesCount(post.getLikedBy().size())
                .isLikedByCurrentUser(currentUsername != null &&
                        post.getLikedBy().stream()
                                .anyMatch(user -> user.getUsername().equals(currentUsername)))
                .build();
    }

    public void likePost(Long postId, String username) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Post not found"));
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (post.getLikedBy().add(user)) {
            postRepository.save(post);
        }
    }

    public void unlikePost(Long postId, String username) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Post not found"));
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (post.getLikedBy().remove(user)) {
            postRepository.save(post);
        }
    }
    public void deletePostById(Long postId) {
        postRepository.deleteById(postId);
    }

    public void incrementViews(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Post not found"));
        post.setViews(post.getViews() + 1);
        postRepository.save(post);
    }

    public List<PostDTO> getUserPosts(String username, String currentUsername) {
        return postRepository.findByAuthorUsernameOrderByPublishedAtDesc(username)
                .stream()
                .map(post -> getPostDTO(post, currentUsername))
                .collect(Collectors.toList());
    }

    public Post getPost(Long postId) {
       return postRepository.findById(postId).orElseThrow(() -> new EntityNotFoundException("Post not found"));
    }
}
