package com.example.askfm.repository;

import com.example.askfm.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findByAuthorUsernameOrderByPublishedAtDesc(String username);
}
