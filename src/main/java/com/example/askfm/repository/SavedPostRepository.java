package com.example.askfm.repository;

import com.example.askfm.model.SavedPost;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SavedPostRepository extends JpaRepository<SavedPost, Long> {
    void deleteByPostIdAndUserUsername(Long postId, String username);

    List<SavedPost> findByUserUsernameOrderBySavedAtDesc(String username);

    boolean existsByPostIdAndUserUsername(Long postId, String username);

    void deleteByPostId(Long postId);
}
