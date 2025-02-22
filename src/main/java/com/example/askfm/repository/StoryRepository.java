package com.example.askfm.repository;

import com.example.askfm.model.Story;
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

}
