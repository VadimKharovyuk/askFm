package com.example.askfm.service;

import com.example.askfm.enums.ArticleType;
import com.example.askfm.model.Article;
import com.example.askfm.repository.ArticleRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ArticleService {

    private final ArticleRepository articleRepository;




    /**
     * Получить все статьи
     */
    public List<Article> getAllArticles() {
        log.debug("Getting all articles");
        return articleRepository.findAll(Sort.by(Sort.Direction.DESC, "lastUpdated"));
    }

    /**
     * Получить статью по ID
     */
    public Article getArticleById(Long id) {
        log.debug("Getting article by id: {}", id);
        return articleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Article not found with id: " + id));
    }



    /**
     * Сохранить статью
     */
    @Transactional
    public Article saveArticle(Article article) {
        log.debug("Saving article: {}", article);

        // Проверяем, существует ли уже статья с таким типом
        if (article.getId() == null) {
            articleRepository.findFirstByType(article.getType()).ifPresent(existingArticle -> {
                throw new IllegalStateException("Article with type " + article.getType() + " already exists");
            });
        }

        // Устанавливаем время последнего обновления
        article.setLastUpdated(LocalDateTime.now());

        // Проверяем обязательные поля
        validateArticle(article);

        return articleRepository.save(article);
    }

    /**
     * Удалить статью
     */
    @Transactional
    public void deleteArticle(Long id) {
        log.debug("Deleting article with id: {}", id);

        Article article = getArticleById(id);

        // Проверяем, не является ли статья единственной своего типа
        if (isLastArticleOfType(article)) {
            throw new IllegalStateException("Cannot delete the last article of type: " + article.getType());
        }

        articleRepository.deleteById(id);
    }

    /**
     * Проверить, является ли статья последней своего типа
     */
    private boolean isLastArticleOfType(Article article) {
        return articleRepository.countByType(article.getType()) <= 1;
    }

    /**
     * Валидация статьи
     */
    private void validateArticle(Article article) {
        if (article.getTitle() == null || article.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Article title cannot be empty");
        }

        if (article.getContent() == null || article.getContent().trim().isEmpty()) {
            throw new IllegalArgumentException("Article content cannot be empty");
        }

        if (article.getType() == null) {
            throw new IllegalArgumentException("Article type cannot be null");
        }
    }
}