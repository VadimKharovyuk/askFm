package com.example.askfm.repository;

import com.example.askfm.model.PostReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostReportRepository extends JpaRepository<PostReport, Long> {
    List<PostReport> findByPostIdOrderByCreatedAtDesc(Long postId);

    Page<PostReport> findAllByOrderByCreatedAtDesc(Pageable pageable);

}
