package com.example.askfm.repository;

import com.example.askfm.model.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.nio.channels.FileChannel;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findByAuthorUsernameOrderByPublishedAtDesc(String username);

    @Query("SELECT DISTINCT p FROM Post p JOIN p.tags t WHERE LOWER(t.name) LIKE LOWER(CONCAT('%', :tagName, '%'))")
    Page<Post> findByTagNameContaining(@Param("tagName") String tagName, Pageable pageable);
    // Поиск постов с наибольшим количеством лайков
    @Query("SELECT p FROM Post p ORDER BY SIZE(p.likedBy) DESC")
    Page<Post> findMostLikedPosts(Pageable pageable);

    // Поиск постов с наибольшим количеством просмотров
    @Query("SELECT p FROM Post p JOIN p.views v GROUP BY p ORDER BY COUNT(v) DESC")
    Page<Post> findMostViewedPosts(Pageable pageable);

    // Поиск последних постов за 24 часа
    @Query("SELECT p FROM Post p WHERE p.publishedAt >= :dayAgo")
    Page<Post> findRecentPosts(@Param("dayAgo") LocalDateTime dayAgo, Pageable pageable);

    // Поиск по автору
    Page<Post> findByAuthorUsernameIgnoreCase(String username, Pageable pageable);

    @Query("SELECT DISTINCT p FROM Post p JOIN p.tags t WHERE t.name IN :tagNames")
    Page<Post> findByTagNames(@Param("tagNames") Set<String> tagNames, Pageable pageable);
}
