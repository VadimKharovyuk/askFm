package com.example.askfm.repository;

import com.example.askfm.model.Story;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StoryRepository extends JpaRepository<Story, Long> {
    List<Story> findByCreatedAtAfter(LocalDateTime oneDayAgo);

    // Добавляем метод для поиска историй пользователя
    List<Story> findByUserUsernameOrderByCreatedAtDesc(String username);

    // Метод для получения активных историй пользователя
    @Query("SELECT s FROM Story s WHERE s.user.username = :username AND s.createdAt > :date ORDER BY s.createdAt DESC")
    List<Story> findActiveStoriesByUsername(@Param("username") String username, @Param("date") LocalDateTime date);

    // Новый метод для поиска историй по списку пользователей
    List<Story> findByUserUsernameInAndCreatedAtAfterOrderByCreatedAtDesc(
            List<String> usernames,
            LocalDateTime date
    );



    Page<Story> findByCreatedAtAfterOrderByCreatedAtDesc(
            LocalDateTime date,
            Pageable pageable
    );

    Page<Story> findByUserUsernameInAndCreatedAtAfterOrderByCreatedAtDesc(
            List<String> usernames,
            LocalDateTime date,
            Pageable pageable
    );

    @Query("SELECT s FROM Story s WHERE s.user.username = :username AND s.createdAt > :date ORDER BY s.createdAt DESC")
    Page<Story> findActiveStoriesByUsername(
            @Param("username") String username,
            @Param("date") LocalDateTime date,
            Pageable pageable
    );

    List<Story> findByCreatedAtBefore(LocalDateTime date);

}
