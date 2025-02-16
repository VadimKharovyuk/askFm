package com.example.askfm.service;

import com.example.askfm.dto.PostDTO;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Caching;
import org.springframework.cache.CacheManager;
import com.example.askfm.maper.PostMapper;
import com.example.askfm.model.Post;
import com.example.askfm.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
@Slf4j
@Service
@RequiredArgsConstructor
public class TopLikedPostsService {

    private static final int PREVIEW_SIZE = 3;
    private final PostRepository postRepository;
    private final PostMapper postMapper;
    private final CacheManager cacheManager;


    @SuppressWarnings("unchecked")
    @Transactional(readOnly = true)
    public Page<PostDTO> getAllTopLikedPosts(Pageable pageable, String currentUsername) {
        String cacheKey = "topPosts_" + pageable.getPageNumber() + "_" + pageable.getPageSize();
        Cache cache = cacheManager.getCache("topPosts");

        if (cache != null) {
            Cache.ValueWrapper valueWrapper = cache.get(cacheKey);
            if (valueWrapper != null) {
                Object value = valueWrapper.get();
                if (value instanceof Page<?>) {
                    log.info("🟢 Данные получены из кеша. Ключ: {}", cacheKey);
                    Page<Post> posts = (Page<Post>) value;
                    return posts.map(post -> postMapper.toDto(post, currentUsername));
                }
            }
        }

        log.info("🔴 Данные не найдены в кеше. Ключ: {}. Загрузка из базы данных...", cacheKey);
        LocalDateTime weekAgo = LocalDateTime.now().minusWeeks(1);
        Page<Post> topPosts = postRepository.findTopLikedPosts(weekAgo, pageable);

        if (cache != null) {
            cache.put(cacheKey, topPosts);
            log.info("💾 Данные сохранены в кеш. Ключ: {}", cacheKey);
        }

        return topPosts.map(post -> postMapper.toDto(post, currentUsername));
    }

    @SuppressWarnings("unchecked")
    @Transactional(readOnly = true)
    public List<PostDTO> getTopLikedPostsPreview(String currentUsername) {
        String cacheKey = "topPostsPreview";
        Cache cache = cacheManager.getCache("topPostsPreview");

        if (cache != null) {
            Cache.ValueWrapper valueWrapper = cache.get(cacheKey);
            if (valueWrapper != null) {
                Object value = valueWrapper.get();
                if (value instanceof List<?> &&
                        (!((List<?>) value).isEmpty() && ((List<?>) value).getFirst() instanceof Post)) {
                    log.info("🟢 Предпросмотр получен из кеша. Ключ: {}", cacheKey);
                    List<Post> posts = (List<Post>) value;
                    return postMapper.toDtoList(posts, currentUsername);
                }
            }
        }

        log.info("🔴 Предпросмотр не найден в кеше. Ключ: {}. Загрузка из базы данных...", cacheKey);
        LocalDateTime weekAgo = LocalDateTime.now().minusWeeks(1);
        Pageable previewPageable = PageRequest.of(0, PREVIEW_SIZE);
        Page<Post> topPosts = postRepository.findTopLikedPosts(weekAgo, previewPageable);
        List<Post> posts = topPosts.getContent();

        if (cache != null) {
            cache.put(cacheKey, posts);
            log.info("💾 Предпросмотр сохранен в кеш. Ключ: {}", cacheKey);
        }

        return postMapper.toDtoList(posts, currentUsername);
    }

//    @Transactional(readOnly = true)
//    public Page<PostDTO> getAllTopLikedPosts(Pageable pageable, String currentUsername) {
//        LocalDateTime weekAgo = LocalDateTime.now().minusWeeks(1);
//        Page<Post> topPosts = postRepository.findTopLikedPosts(weekAgo, pageable);
//        return topPosts.map(post -> postMapper.toDto(post, currentUsername));
//    }
//
//
//    @Transactional(readOnly = true)
//    public List<PostDTO> getTopLikedPostsPreview(String currentUsername) {
//        LocalDateTime weekAgo = LocalDateTime.now().minusWeeks(1);
//        Pageable previewPageable = PageRequest.of(0, PREVIEW_SIZE);
//        Page<Post> topPosts = postRepository.findTopLikedPosts(weekAgo, previewPageable);
//        return postMapper.toDtoList(topPosts.getContent(), currentUsername);
//    }
}