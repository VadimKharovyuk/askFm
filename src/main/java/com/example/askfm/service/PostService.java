package com.example.askfm.service;

import com.example.askfm.dto.PostCreateDTO;
import com.example.askfm.dto.PostDTO;
import com.example.askfm.exception.PostNotFoundException;
import com.example.askfm.model.Post;
import com.example.askfm.model.PostView;
import com.example.askfm.model.Tag;
import com.example.askfm.model.User;
import com.example.askfm.repository.PostRepository;

import com.example.askfm.repository.PostViewRepository;
import com.example.askfm.repository.TagRepository;
import com.example.askfm.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    private final PostViewRepository postViewRepository;
    private final TagRepository tagRepository;
    private final EntityManager entityManager;

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

        Set<Tag> tags = Arrays.stream(postDTO.getTags().split(","))
                .map(String::trim)
                .filter(tag -> !tag.isEmpty())
                .map(tagName -> tagRepository.findByName(tagName)
                        .orElseGet(() -> tagRepository.save(Tag.builder().name(tagName).build())))
                .collect(Collectors.toSet());

        Post post = Post.builder()
                .author(author)
                .content(postDTO.getContent())
                .media(mediaBytes)
                .publishedAt(LocalDateTime.now())
                .tags(tags)
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
                .tags(new HashSet<>(post.getTags().stream()
                        .map(Tag::getName)
                        .collect(Collectors.toSet())))
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


    @Transactional
    public void deletePost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException("Post not found: " + postId));

        postViewRepository.deleteByPostId(postId);
        tagRepository.deleteByPostId(postId);
        postRepository.delete(post);
    }


    @Transactional
    public void incrementViews(Long postId, String username) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Post not found"));
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (postViewRepository.findByPostAndUser(post, user).isEmpty()) {
            PostView view = PostView.builder()
                    .post(post)
                    .user(user)
                    .viewedAt(LocalDateTime.now())
                    .build();
            postViewRepository.save(view);
        }
    }

    public long getPostViews(Long postId) {
        return postViewRepository.countByPostId(postId);
    }

    public List<PostDTO> getUserPostsWiews(String username, String currentUsername) {
        return postRepository.findByAuthorUsernameOrderByPublishedAtDesc(username)
                .stream()
                .map(post -> {
                    PostDTO dto = getPostDTO(post, currentUsername);
                    dto.setViews(getPostViews(post.getId()));
                    return dto;
                })
                .collect(Collectors.toList());
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


    public Page<PostDTO> findPostsByTags(String tags, String currentUsername, Pageable pageable) {
        if (tags == null || tags.trim().isEmpty()) {
            return Page.empty(pageable);
        }

        // Убираем скобки и обрабатываем тег
        String cleanTag = tags.replace("[", "").replace("]", "").trim();
        Page<Post> posts = postRepository.findByTagNameContaining(cleanTag, pageable);
        return posts.map(post -> getPostDTO(post, currentUsername));
    }
}
