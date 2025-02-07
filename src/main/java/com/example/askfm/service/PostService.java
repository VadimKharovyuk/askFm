
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
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
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
    private final CacheManager cacheManager;
    private final NotificationService notificationService;


    public List<PostDTO> getUserPosts(String username, String currentUsername) {
        Cache cache = cacheManager.getCache("posts");
        String cacheKey = "posts:" + username + ":" + currentUsername;

        List<PostDTO> cachedPosts = cache != null ?
                (List<PostDTO>) cache.get(cacheKey, List.class) : null;

        if (cachedPosts != null) {
            log.debug("✅ Получены посты из кеша для пользователя: {}, запрошено пользователем: {}, количество: {}",
                    username, currentUsername, cachedPosts.size());
            return cachedPosts;
        }

        log.debug("⛔ Поиск в БД: Получение постов для пользователя: {}", username);

        List<Post> originalPosts = postRepository.findByAuthorUsernameOrderByPublishedAtDesc(username);
        log.debug("🔹 Найдено {} оригинальных постов", originalPosts.size());

        List<Repost> reposts = repostRepository.findByUserUsernameOrderByRepostedAtDesc(username);
        log.debug("🔹 Найдено {} репостов", reposts.size());

        List<PostDTO> allPosts = new ArrayList<>();

        // Добавляем оригинальные посты
        allPosts.addAll(originalPosts.stream()
                .map(post -> postMapper.toDto(post, currentUsername))
                .toList());

        // Добавляем репосты
        allPosts.addAll(reposts.stream()
                .map(repost -> postMapper.toDtoWithRepost(
                        repost.getOriginalPost(),
                        currentUsername,
                        username,
                        repost.getRepostedAt()))
                .toList());

        // Сортируем все посты по дате
        List<PostDTO> sortedPosts = allPosts.stream()
                .sorted(Comparator.comparing(post ->
                                post.getRepostedAt() != null ? post.getRepostedAt() : post.getPublishedAt(),
                        Comparator.reverseOrder()))
                .collect(Collectors.toList());

        cache.put(cacheKey, sortedPosts);
        log.debug("💾 Сохранено в кеш {} постов для пользователя: {} (ключ: {})",
                sortedPosts.size(), username, cacheKey);

        return sortedPosts;
    }


    public Post createPost(String username, PostCreateDTO postDTO) {
        log.debug("📝 Создание нового поста для пользователя: {}", username);

        User author = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.error("❌ Пользователь не найден при создании поста: {}", username);
                    return new UsernameNotFoundException("User not found: " + username);
                });

        byte[] mediaBytes = processMedia(postDTO);
        log.debug("🖼️ Обработаны медиа файлы для поста");

        Set<Tag> tags = processTags(postDTO.getTags());
        log.debug("🏷️ Обработано {} тегов для поста", tags.size());

        Post post = Post.builder()
                .author(author)
                .content(postDTO.getContent())
                .media(mediaBytes)
                .publishedAt(LocalDateTime.now())
                .tags(tags)
                .build();

        Set<User> mentionedUsers = mentionService.extractMentions(postDTO.getContent());
        post.setMentionedUsers(mentionedUsers);
        log.debug("👥 Извлечено {} упоминаний из контента поста", mentionedUsers.size());

        Post savedPost = postRepository.save(post);
        log.debug("✨ Успешно создан пост с ID: {}", savedPost.getId());

        // Очищаем кеш постов после создания нового поста
        Cache cache = cacheManager.getCache("posts");
        if (cache != null) {
            cache.clear();
            log.debug("🧹 Кеш постов очищен после создания нового поста");
        }

        return savedPost;
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

    //    @Transactional
//    public void likePost(Long postId, String username) {
//        log.debug("📝 Начало процесса лайка поста: {} пользователем: {}", postId, username);
//
//        Post post = findById(postId);
//        User user = userRepository.findByUsername(username)
//                .orElseThrow(() -> {
//                    log.error("❌ Пользователь не найден: {}", username);
//                    return new UsernameNotFoundException("User not found: " + username);
//                });
//
//        try {
//            // Проверяем, что пользователь не лайкает свой собственный пост
//            if (!post.getAuthor().equals(user)) {
//                notificationService.notifyAboutLike(user, post);
//                log.debug("📨 Отправлено уведомление о лайке автору поста: {}",
//                        post.getAuthor().getUsername());
//            }
//
//            // Добавляем лайк
//            if (post.getLikedBy().add(user)) {
//                postRepository.save(post);
//                log.debug("✅ Лайк успешно добавлен к посту: {}", postId);
//            } else {
//                log.debug("ℹ️ Пользователь уже лайкнул этот пост");
//            }
//
//        } catch (Exception e) {
//            log.error("❌ Ошибка при добавлении лайка к посту {}: {}",
//                    postId, e.getMessage());
//            throw e;
//        }
//    }
//
//    private Post findById(Long postId) {
//        return postRepository.findById(postId)
//                .orElseThrow(() -> {
//                    log.error("❌ Пост не найден: {}", postId);
//                    return new PostNotFoundException("Post not found with id: " + postId);
//                });
//    }
    @Transactional
    public void likePost(Long postId, String username) {

        Post post = findById(postId);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        notificationService.notifyAboutLike(user, post);
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
        log.debug("📝 Начало удаления поста: {}", postId);

        Post post = findById(postId);
        String authorUsername = post.getAuthor().getUsername();

        postViewRepository.deleteByPostId(postId);
        log.debug("👁️ Удалены связанные просмотры для поста: {}", postId);

        tagRepository.deleteByPostId(postId);
        log.debug("🏷️ Удалены связанные теги для поста: {}", postId);

        postRepository.delete(post);
        log.debug("✨ Пост удален из БД: {}", postId);

        // Очистка кеша
        Cache cache = cacheManager.getCache("posts");
        if (cache != null) {
            // Очищаем кеш постов автора
            String authorCacheKey = "posts:" + authorUsername + ":*";
            cache.evict(authorCacheKey);
            log.debug("🧹 Очищен кеш постов автора: {}", authorUsername);

            // Очищаем общий кеш
            cache.clear();
            log.debug("🧹 Очищен общий кеш постов");
        }

        log.info("✅ Пост {} пользователя {} успешно удален", postId, authorUsername);
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
        Cache viewsCache = cacheManager.getCache("views");
        String cacheKey = "post_views_count:" + postId;

        Long viewsCount = viewsCache.get(cacheKey, Long.class);
        if (viewsCount != null) {
            log.debug("✅ Взято з кешу кількість переглядів для поста {}: {}", postId, viewsCount);
            return viewsCount;
        }

        log.debug("⛔ Підрахунок переглядів в БД для поста {}", postId);
        viewsCount = postViewRepository.countByPostId(postId);

        viewsCache.put(cacheKey, viewsCount);
        log.debug("🔹 Знайдено в БД {} переглядів для поста {}", viewsCount, postId);
        return viewsCount;
    }
//
//    public long getPostViews(Long postId) {
//        return postViewRepository.countByPostId(postId);
//    }


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
