package com.example.askfm.repository;

import com.example.askfm.model.Post;
import com.example.askfm.model.Repost;
import com.example.askfm.model.User;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface RepostRepository extends JpaRepository<Repost, Long> {


    @Query("SELECT COUNT(r) FROM Repost r WHERE r.originalPost.id = :postId")
    Long countByPostId(@Param("postId") Long postId);

    @Query("SELECT r FROM Repost r WHERE r.user.id = :userId AND r.originalPost.id = :postId")
    Optional<Repost> findByUserIdAndPostId(@Param("userId") Long userId, @Param("postId") Long postId);

    void deleteByUserIdAndOriginalPostId(Long userId, Long postId);


    List<Repost> findByUserUsernameOrderByRepostedAtDesc(String username);
    boolean existsByUserAndOriginalPost(User user, Post originalPost);
    List<Repost> findByUserId(@Param("userId") Long userId, Pageable pageable);
    List<Repost> findByOriginalPostId(@Param("postId") Long postId, Pageable pageable);

    Optional<Repost> findByUserIdAndOriginalPostId(Long userId, Long originalPostId);

    void deleteByOriginalPostId(Long postId);

}
