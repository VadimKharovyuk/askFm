package com.example.askfm.repository;

import com.example.askfm.model.GroupPostComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface GroupPostCommentRepository extends JpaRepository<GroupPostComment, Long> {
    Page<GroupPostComment> findByPostIdOrderByCreatedAtDesc(Long postId, Pageable pageable);
}
