package com.example.askfm.service;

import com.example.askfm.dto.NewsDTO;
import com.example.askfm.enums.NewsCategory;
import com.example.askfm.enums.UserRole;
import com.example.askfm.model.News;
import com.example.askfm.model.User;
import com.example.askfm.repository.NewsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NewsService {
    private final NewsRepository newsRepository;
    private final ImageService imageService;
    private final UserService userService;

    public News createNews(NewsDTO newsDTO, String authorUsername) {
        User author = userService.findByUsername(authorUsername);

        // Проверка прав на создание новости
        if (author.getRole() == UserRole.USER) {
            throw new IllegalStateException("Only moderators and admins can create news");
        }

        News news = News.builder()
                .title(newsDTO.getTitle())
                .content(newsDTO.getContent())
                .category(newsDTO.getCategory())
                .author(author)
                .createdAt(LocalDateTime.now())
                .viewCount(0L)
                .published(true)
                .build();

        if (newsDTO.getThumbnail() != null) {
            try {
                byte[] resizedThumbnail = imageService.resizeImage(newsDTO.getThumbnail().getBytes(), 800);
                news.setThumbnail(resizedThumbnail);
            } catch (IOException e) {
                throw new RuntimeException("Error processing thumbnail", e);
            }
        }

        return newsRepository.save(news);
    }

    public News updateNews(Long id, NewsDTO newsDTO) {
        News news = newsRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("News not found"));

        news.setTitle(newsDTO.getTitle());
        news.setContent(newsDTO.getContent());
        news.setCategory(newsDTO.getCategory());

        if (newsDTO.getThumbnail() != null) {
            try {
                byte[] resizedThumbnail = imageService.resizeImage(newsDTO.getThumbnail().getBytes(), 800);
                news.setThumbnail(resizedThumbnail);
            } catch (IOException e) {
                throw new RuntimeException("Error processing thumbnail", e);
            }
        }

        return newsRepository.save(news);
    }



    public List<News> getRecentNews() {
        return newsRepository.findTop5ByPublishedTrueOrderByCreatedAtDesc();
    }

    public long getTotalNewsCount() {
        return newsRepository.count();
    }

    @Transactional(readOnly = true)
    public News getNewsById(Long id) {
        return newsRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("News not found"));
    }

    @Transactional
    public void incrementViewCount(Long newsId) {
        newsRepository.incrementViewCount(newsId);
    }

    public List<News> getNewsByCategory(NewsCategory category) {
        return newsRepository.findByCategoryAndPublishedTrueOrderByCreatedAtDesc(category);
    }



    public List<News> getAllPublishedNews() {
        return newsRepository.findByPublishedTrueOrderByCreatedAtDesc();
    }






    public NewsDTO convertToDTO(News news) {
        return NewsDTO.builder()
                .id(news.getId())
                .title(news.getTitle())
                .content(news.getContent())
                .category(news.getCategory())
                .viewCount(news.getViewCount())
                .thumbnailBase64(news.getThumbnail() != null ?
                        imageService.getBase64Avatar(news.getThumbnail()) : null)
                .authorUsername(news.getAuthor().getUsername())
                .createdAt(news.getCreatedAt())
                .published(news.isPublished())
                .build();
    }



}
