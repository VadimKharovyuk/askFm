
package com.example.askfm.service;

import com.example.askfm.dto.PostDTO;
import com.example.askfm.maper.PostMapper;
import com.example.askfm.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SearchService {
    private final PostRepository postRepository;
    private final PostMapper postMapper;

    private static final int DEFAULT_TOP_POSTS_COUNT = 5;

    public List<PostDTO> getMostLikedPosts(String currentUsername) {
        log.debug("Fetching most liked posts for current user: {}", currentUsername);
        Pageable topPosts = createPageableForTopPosts(Sort.by("likedBy").descending());

        return postRepository.findMostLikedPosts(topPosts)
                .map(post -> postMapper.toDto(post, currentUsername))
                .getContent();
    }

    public List<PostDTO> getMostViewedPosts(String currentUsername) {
        log.debug("Fetching most viewed posts for current user: {}", currentUsername);
        Pageable topPosts = createPageableForTopPosts(Sort.unsorted());

        return postRepository.findMostViewedPosts(topPosts)
                .map(post -> postMapper.toDto(post, currentUsername))
                .getContent();
    }

    public List<PostDTO> getRecentPosts(String currentUsername) {
        log.debug("Fetching recent posts for current user: {}", currentUsername);
        LocalDateTime dayAgo = LocalDateTime.now().minusDays(1);
        Pageable recentPosts = createPageableForTopPosts(Sort.by("publishedAt").descending());

        return postRepository.findRecentPosts(dayAgo, recentPosts)
                .map(post -> postMapper.toDto(post, currentUsername))
                .getContent();
    }

    public Page<PostDTO> searchByAuthor(String username, String currentUsername, Pageable pageable) {
        log.debug("Searching posts by author: {} for current user: {}", username, currentUsername);
        return postRepository.findByAuthorUsernameIgnoreCase(username, pageable)
                .map(post -> postMapper.toDto(post, currentUsername));
    }

    public Page<PostDTO> findPostsByTags(String tags, String currentUsername, Pageable pageable) {
        if (!StringUtils.hasText(tags)) {
            log.debug("No tags provided for search");
            return Page.empty(pageable);
        }

        Set<String> tagNames = parseTagsString(tags);
        log.debug("Searching posts by tags: {} for current user: {}", tagNames, currentUsername);

        return postRepository.findByTagNames(tagNames, pageable)
                .map(post -> postMapper.toDto(post, currentUsername));
    }

    private Pageable createPageableForTopPosts(Sort sort) {
        return PageRequest.of(0, DEFAULT_TOP_POSTS_COUNT, sort);
    }

    private Set<String> parseTagsString(String tags) {
        return Arrays.stream(tags.split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .collect(Collectors.toSet());
    }
}
