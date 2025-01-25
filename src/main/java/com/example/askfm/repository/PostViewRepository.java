package com.example.askfm.repository;

import com.example.askfm.model.Post;
import com.example.askfm.model.PostView;
import com.example.askfm.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.Optional;

public interface PostViewRepository extends JpaRepository<PostView, Long> {
    Optional<PostView> findByPostAndUser(Post post, User user);

    long countByPostId(Long postId);

    void deleteByPostId(Long postId);

}
