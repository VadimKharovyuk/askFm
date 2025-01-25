package com.example.askfm.repository;

import com.example.askfm.model.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, Long> {
    Optional<Tag> findByName(String tagName);

    @Modifying
    @Query(value = "DELETE FROM post_tags WHERE post_id = :postId", nativeQuery = true)
    void deleteByPostId(@Param("postId") Long postId);

}
