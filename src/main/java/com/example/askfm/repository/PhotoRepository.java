package com.example.askfm.repository;

import com.example.askfm.model.Photo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PhotoRepository extends JpaRepository<Photo, Long> {
    @Query("SELECT p FROM Photo p WHERE p.owner.id = :userId ORDER BY p.createdAt DESC")
    Page<Photo> findByOwnerId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT p FROM Photo p WHERE p.owner.username = :username ORDER BY p.createdAt DESC")
    Page<Photo> findByOwnerUsername(@Param("username") String username, Pageable pageable);
}
