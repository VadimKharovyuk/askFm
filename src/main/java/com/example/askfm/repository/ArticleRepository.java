package com.example.askfm.repository;

import com.example.askfm.enums.ArticleType;
import com.example.askfm.model.Article;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ArticleRepository extends JpaRepository<Article, Long> {

  Optional<Article> findFirstByType(ArticleType type);


  @Query("SELECT a FROM Article a WHERE LOWER(a.title) LIKE LOWER(concat('%', :query, '%')) " +
          "OR LOWER(a.content) LIKE LOWER(concat('%', :query, '%'))")
  List<Article> searchArticles(@Param("query") String query);

  Optional<Article> findByType(ArticleType articleType);

  // Добавим метод для поиска последней версии статьи по типу
  // Поиск последней версии по типу
  @Query("SELECT a FROM Article a WHERE a.type = :type ORDER BY a.lastUpdated DESC LIMIT 1")
  Optional<Article> findLatestByType(@Param("type") ArticleType type);

  // Поиск всех версий определенного типа, отсортированных по дате
  List<Article> findByTypeOrderByLastUpdatedDesc(ArticleType type);

  // Подсчет количества версий определенного типа
  long countByType(ArticleType type);

}
