
package com.example.askfm.service;

import com.example.askfm.dto.PostCreateDTO;
import com.example.askfm.dto.PostDTO;
import com.example.askfm.exception.PostNotFoundException;
import com.example.askfm.maper.PostMapper;
import com.example.askfm.model.*;
import com.example.askfm.repository.*;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.constraints.NotNull;
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
    private final MentionService mentionService;
    private final PostReportRepository postReportRepository;
    private final RepostRepository repostRepository;
    private final PostMapper postMapper;

    public List<PostDTO> getUserPosts(String username, String currentUsername) {

        List<Post> originalPosts = postRepository.findByAuthorUsernameOrderByPublishedAtDesc(username);
        List<Repost> reposts = repostRepository.findByUserUsernameOrderByRepostedAtDesc(username);

        List<PostDTO> allPosts = new ArrayList<>();

        // Add original posts
        allPosts.addAll(originalPosts.stream()
                .map(post -> postMapper.toDto(post, currentUsername))
                .toList());

        // Add reposts
        allPosts.addAll(reposts.stream()
                .map(repost -> postMapper.toDtoWithRepost(
                        repost.getOriginalPost(),
                        currentUsername,
                        username,
                        repost.getRepostedAt()))
                .toList());

        // Sort all posts by published/reposted date
        return allPosts.stream()
                .sorted(Comparator.comparing(post ->
                                post.getRepostedAt() != null ? post.getRepostedAt() : post.getPublishedAt(),
                        Comparator.reverseOrder()))
                .collect(Collectors.toList());
    }

    public Post createPost(String username, PostCreateDTO postDTO) {

        User author = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        byte[] mediaBytes = processMedia(postDTO);
        Set<Tag> tags = processTags(postDTO.getTags());

        Post post = Post.builder()
                .author(author)
                .content(postDTO.getContent())
                .media(mediaBytes)
                .publishedAt(LocalDateTime.now())
                .tags(tags)
                .build();

        post.setMentionedUsers(mentionService.extractMentions(postDTO.getContent()));

        return postRepository.save(post);
    }

    private byte[] processMedia(PostCreateDTO postDTO) {
        if (postDTO.getMedia() != null && !postDTO.getMedia().isEmpty()) {
            try {
                return imageService.resizeImage(postDTO.getMedia().getBytes(), 800);
            } catch (IOException e) {
                throw new RuntimeException("Failed to process image", e);
            }
        }
        return null;
    }

    private Set<Tag> processTags(String tagsString) {
        return Arrays.stream(tagsString.split(","))
                .map(String::trim)
                .filter(tag -> !tag.isEmpty())
                .map(tagName -> tagRepository.findByName(tagName)
                        .orElseGet(() -> tagRepository.save(Tag.builder().name(tagName).build())))
                .collect(Collectors.toSet());
    }

    @Transactional
    public void likePost(Long postId, String username) {
        Post post = findById(postId);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (post.getLikedBy().add(user)) {
            postRepository.save(post);
        }
    }

    @Transactional
    public void unlikePost(Long postId, String username) {
        Post post = findById(postId);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (post.getLikedBy().remove(user)) {
            postRepository.save(post);
            log.debug("User {} unliked post {}", username, postId);
        }
    }

    @Transactional
    public void deletePostAdmin(Long postId) {
        log.debug("Admin deleting post: {}", postId);
        postViewRepository.deleteByPostId(postId);
        tagRepository.deleteByPostId(postId);
        postRepository.deleteById(postId);
        postReportRepository.deleteById(postId);
    }

    @Transactional
    public void deletePost(Long postId) {
        log.debug("Deleting post: {}", postId);
        Post post = findById(postId);
        postViewRepository.deleteByPostId(postId);
        tagRepository.deleteByPostId(postId);
        postRepository.delete(post);
    }

    @Transactional
    public void incrementViews(Long postId, String username) {
        Post post = findById(postId);
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


    public Post getPost(Long postId) {
        return findById(postId);
    }

    public Page<PostDTO> findPostsByTags(String tags, String currentUsername, Pageable pageable) {
        if (tags == null || tags.trim().isEmpty()) {
            return Page.empty(pageable);
        }

        String cleanTag = tags.replace("[", "").replace("]", "").trim();
        Page<Post> posts = postRepository.findByTagNameContaining(cleanTag, pageable);
        return posts.map(post -> postMapper.toDto(post, currentUsername));
    }

    @Transactional(readOnly = true)
    public PostDTO getPostDetails(Long postId, String currentUsername) {

        Post post = findById(postId);

        // Получаем базовый PostDTO через маппер
        PostDTO postDTO = postMapper.toDto(post, currentUsername);

        // Если это репост, добавляем информацию о репосте
        if (!post.getReposts().isEmpty()) {
            Repost repost = post.getReposts().get(0);
            postDTO.setRepostedBy(repost.getUser().getUsername());
            postDTO.setRepostedAt(repost.getRepostedAt());
            postDTO.setOriginalAuthorUsername(post.getAuthor().getUsername());
            postDTO.setOriginalPublishedAt(post.getPublishedAt());
        }

        return postDTO;
    }
    public Post findById(@NotNull(message = "ID поста обязателен") Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException(postId));
    }
}
