package com.example.askfm.repository;

import com.example.askfm.model.GroupPost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupPostRepository extends JpaRepository<GroupPost, Long> {
    Page<GroupPost> findByGroupIdOrderByPublishedAtDesc(Long groupId, Pageable pageable);
}
