package com.example.askfm.repository;

import com.example.askfm.model.AdLead;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AdLeadRepository extends JpaRepository<AdLead, Long> {

    Page<AdLead> findByAdId(Long adId, Pageable pageable);

    long countByAdId(Long id);

}
