package com.example.askfm.repository;

import com.example.askfm.model.Ad;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface AdRepository extends JpaRepository<Ad, Long> {
    List<Ad> findByIsActiveTrue();

    List<Ad> findByCreatedById(Long userId);

    List<Ad> findByCreatedByIdAndIsActiveTrue(Long userId);

}
