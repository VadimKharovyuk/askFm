package com.example.askfm.repository;

import com.example.askfm.enums.NewsCategory;
import com.example.askfm.model.News;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NewsRepository extends JpaRepository<News, Long> {
    List<News> findByCategoryOrderByCreatedAtDesc(NewsCategory category);

    List<News> findTop5ByPublishedTrueOrderByCreatedAtDesc();

    @Modifying
    @Query("UPDATE News n SET n.viewCount = n.viewCount + 1 WHERE n.id = :newsId")
    void incrementViewCount(@Param("newsId") Long newsId);

    List<News> findByPublishedTrueOrderByCreatedAtDesc();

    List<News> findByCategoryAndPublishedTrueOrderByCreatedAtDesc(NewsCategory category);




}
