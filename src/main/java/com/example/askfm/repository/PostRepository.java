package com.example.askfm.repository;

import com.example.askfm.model.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findByAuthorUsernameOrderByPublishedAtDesc(String username);

    @Query("SELECT DISTINCT p FROM Post p JOIN p.tags t WHERE LOWER(t.name) LIKE LOWER(CONCAT('%', :tagName, '%'))")
    Page<Post> findByTagNameContaining(@Param("tagName") String tagName, Pageable pageable);


}
