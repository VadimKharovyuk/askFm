package com.example.askfm.repository;

import com.example.askfm.model.Photo;
import com.example.askfm.model.UnlockedPhoto;
import com.example.askfm.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UnlockedPhotoRepository extends JpaRepository<UnlockedPhoto, Long> {
    boolean existsByUserAndPhoto(User user, Photo photo);
    boolean existsByUserIdAndPhotoId(Long userId, Long photoId);
}