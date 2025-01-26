package com.example.askfm.service;

import com.example.askfm.dto.PostDTO;

import com.example.askfm.model.Post;
import com.example.askfm.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final PostRepository postRepository;
    private final PostService postService;

    // Популярные посты за последние 24 часа
    public List<PostDTO> getMostLikedPosts(String currentUsername) {
        Pageable topPosts = PageRequest.of(0, 5, Sort.by("likedBy").descending());
        return postRepository.findMostLikedPosts(topPosts)
                .map(post -> postService.getPostDTO(post, currentUsername))
                .getContent();
    }

    // Самые просматриваемые посты
    public List<PostDTO> getMostViewedPosts(String currentUsername) {
        Pageable topPosts = PageRequest.of(0, 5);
        return postRepository.findMostViewedPosts(topPosts)
                .map(post -> postService.getPostDTO(post, currentUsername))
                .getContent();
    }

    // Последние посты
    public List<PostDTO> getRecentPosts(String currentUsername) {
        LocalDateTime dayAgo = LocalDateTime.now().minusDays(1);
        Pageable recentPosts = PageRequest.of(0, 5, Sort.by("publishedAt").descending());
        return postRepository.findRecentPosts(dayAgo, recentPosts)
                .map(post -> postService.getPostDTO(post, currentUsername))
                .getContent();
    }

    // Поиск по автору
    public Page<PostDTO> searchByAuthor(String username, String currentUsername, Pageable pageable) {
        return postRepository.findByAuthorUsernameIgnoreCase(username, pageable)
                .map(post -> postService.getPostDTO(post, currentUsername));
    }



    // Поиск по тегам
    public Page<PostDTO> findPostsByTags(String tags, String currentUsername, Pageable pageable) {
        if (tags == null || tags.trim().isEmpty()) {
            return Page.empty(pageable);
        }

        Set<String> tagNames = Arrays.stream(tags.split(","))
                .map(String::trim)
                .filter(tag -> !tag.isEmpty())
                .collect(Collectors.toSet());

        return postRepository.findByTagNames(tagNames, pageable)
                .map(post -> postService.getPostDTO(post, currentUsername));
    }


}
