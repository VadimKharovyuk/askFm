package com.example.askfm.repository;

import com.example.askfm.model.NewsletterSubscriber;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NewsletterSubscriberRepository extends JpaRepository<NewsletterSubscriber, Long> {
    // Находит всех активных подписчиков
    List<NewsletterSubscriber> findByIsActiveTrue();

    // Проверяет существование подписчика по email
    boolean existsByEmail(String email);

    // Находит подписчика по email
    Optional<NewsletterSubscriber> findByEmail(String email);
}