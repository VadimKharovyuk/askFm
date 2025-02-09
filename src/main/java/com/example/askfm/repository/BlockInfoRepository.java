package com.example.askfm.repository;

import com.example.askfm.model.BlockInfo;
import com.example.askfm.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface BlockInfoRepository extends JpaRepository<BlockInfo, Long> {
    Page<BlockInfo> findByBlocker(User blocker, Pageable pageable);
    void deleteByBlockerAndBlocked(User blocker, User blocked);
    boolean existsByBlockerAndBlocked(User blocker, User blocked);

    long countByBlocker(User user);

}
